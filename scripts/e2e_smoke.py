#!/usr/bin/env python3
"""End-to-end smoke test for the HMS stack running under docker compose.

Walks a realistic clinical flow through the API gateway with one user per role, then checks
the async side effects (RabbitMQ -> notification-service) and the web-ui. Standard library only.

    docker compose up -d
    python3 scripts/e2e_smoke.py [--gateway http://localhost:8080] [--web http://localhost:8090]

Exits non-zero if any step fails.
"""
import argparse
import datetime as dt
import http.cookiejar
import json
import re
import sys
import time
import urllib.error
import urllib.parse
import urllib.request

PASSWORD = "Passw0rd!123"
RESULTS = []


class Api:
    def __init__(self, base):
        self.base = base.rstrip("/")

    def call(self, method, path, token=None, body=None, raw=False):
        data = json.dumps(body).encode() if body is not None else None
        req = urllib.request.Request(self.base + path, data=data, method=method)
        req.add_header("Content-Type", "application/json")
        if token:
            req.add_header("Authorization", "Bearer " + token)
        try:
            with urllib.request.urlopen(req, timeout=30) as resp:
                payload = resp.read()
                status = resp.status
        except urllib.error.HTTPError as e:
            payload, status = e.read(), e.code
        if raw:
            return status, payload
        try:
            return status, json.loads(payload) if payload else None
        except ValueError:
            return status, payload.decode(errors="replace")


def step(name, status, body, expect=(200, 201)):
    expect = (expect,) if isinstance(expect, int) else expect
    ok = status in expect
    RESULTS.append((ok, name, status))
    detail = ""
    if not ok:
        detail = " -> " + (json.dumps(body)[:400] if not isinstance(body, (bytes, str)) else str(body)[:400])
    print(f"  [{'PASS' if ok else 'FAIL'}] {status} {name}{detail}")
    return body.get("data") if ok and isinstance(body, dict) else None


def check(name, cond, info=""):
    RESULTS.append((bool(cond), name, "-"))
    print(f"  [{'PASS' if cond else 'FAIL'}]  -  {name}{(' -> ' + info) if (info and not cond) else ''}")
    return cond


def next_weekday(days_ahead=3):
    d = dt.date.today() + dt.timedelta(days=days_ahead)
    while d.weekday() >= 5:
        d += dt.timedelta(days=1)
    return d


def run_api(api, run_id, admin_email, admin_password):
    roles = ["ADMIN", "RECEPTIONIST", "DOCTOR", "NURSE", "LAB_TECH", "PHARMACIST", "ACCOUNTANT", "PATIENT"]
    users, tokens = {}, {}

    print("\n== Auth")
    s, body = api.call("POST", "/api/v1/auth/login", body={"usernameOrEmail": admin_email, "password": admin_password})
    bootstrap_token = (step("login as bootstrap admin", s, body) or {}).get("accessToken")
    s, body = api.call("POST", "/api/v1/auth/register", body={
        "username": f"rogue_{run_id}", "email": f"rogue_{run_id}@hms.local", "password": PASSWORD,
        "firstName": "Rogue", "lastName": "E2E", "role": "ADMIN"})
    step("anonymous self-registration as ADMIN refused", s, body, expect=403)
    for role in roles:
        uname = f"{role.lower()}_{run_id}"
        # Only an existing admin may create an ADMIN account.
        _, body = api.call("POST", "/api/v1/auth/register", bootstrap_token if role == "ADMIN" else None, body={
            "username": uname, "email": f"{uname}@hms.local", "password": PASSWORD,
            "firstName": role.title(), "lastName": "E2E", "role": role})
        users[role] = step(f"register {role}", _, body) or {}
        s, body = api.call("POST", "/api/v1/auth/login",
                           body={"usernameOrEmail": f"{uname}@hms.local", "password": PASSWORD})
        data = step(f"login {role}", s, body) or {}
        tokens[role] = data.get("accessToken")
    T = tokens
    s, body = api.call("GET", "/api/v1/auth/me", T["DOCTOR"])
    me = step("me (doctor)", s, body) or {}
    check("me returns DOCTOR role", me.get("role") == "DOCTOR", str(me))
    s, body = api.call("GET", "/api/v1/patients", None)
    step("unauthenticated request rejected", s, body, expect=401)

    print("\n== Doctor")
    s, body = api.call("POST", "/api/v1/doctors", T["ADMIN"], {
        "userId": users["DOCTOR"].get("id"), "firstName": "Asha", "lastName": "Rao",
        "registrationNo": f"KMC-{run_id}", "qualification": "MBBS, MD",
        "specialisation": "Cardiology", "department": "Cardiology", "consultationFee": 500.0})
    doctor = step("admin creates doctor profile", s, body) or {}
    doctor_id = doctor.get("id")
    day = next_weekday()
    s, body = api.call("PUT", f"/api/v1/doctors/{doctor_id}/schedule", T["DOCTOR"], {"slots": [
        {"dayOfWeek": day.strftime("%A").upper(), "startTime": "09:00", "endTime": "13:00", "slotDurationMin": 15}]})
    step("doctor sets weekly schedule", s, body)
    s, body = api.call("GET", f"/api/v1/doctors/{doctor_id}/availability?date={day}", T["PATIENT"])
    slots = step("patient views availability", s, body)
    check("availability has slots", bool(slots), str(slots)[:200])
    s, body = api.call("GET", "/api/v1/doctors?specialisation=Cardiology", T["PATIENT"])
    step("filter doctors", s, body)

    print("\n== Patient")
    s, body = api.call("POST", "/api/v1/patients", T["RECEPTIONIST"], {
        "firstName": "John", "lastName": f"Doe{run_id}", "dob": "1990-05-12", "gender": "MALE",
        "bloodGroup": "O+", "phone": "+91-9000000000", "email": f"john{run_id}@example.com",
        "address": "12 MG Road, Bengaluru", "userId": users["PATIENT"].get("id")})
    patient = step("receptionist registers patient", s, body) or {}
    pid = patient.get("id")
    s, body = api.call("GET", f"/api/v1/patients/{pid}", T["PATIENT"])
    step("patient reads own profile", s, body)
    s, body = api.call("GET", f"/api/v1/patients?search=Doe{run_id}&page=0&size=20", T["NURSE"])
    step("nurse searches patients", s, body)
    s, body = api.call("PUT", f"/api/v1/patients/{pid}", T["RECEPTIONIST"], {"phone": "+91-9111111111"})
    step("update patient", s, body)
    s, body = api.call("POST", f"/api/v1/patients/{pid}/allergies", T["NURSE"],
                       {"allergen": "Penicillin", "severity": "HIGH", "notedOn": "2026-01-10"})
    step("add allergy", s, body)
    s, body = api.call("GET", f"/api/v1/patients/{pid}/allergies", T["PATIENT"])
    step("list allergies", s, body)
    s, body = api.call("POST", f"/api/v1/patients/{pid}/emergency-contacts", T["NURSE"],
                       {"name": "Jane Doe", "relation": "Spouse", "phone": "+91-9222222222"})
    step("add emergency contact", s, body)
    s, body = api.call("GET", f"/api/v1/patients/{pid}/emergency-contacts", T["PATIENT"])
    step("list emergency contacts", s, body)
    s, body = api.call("POST", f"/api/v1/patients/{pid}/admit", T["RECEPTIONIST"], {"wardId": "GEN-A", "bedNo": "12"})
    step("admit to IPD", s, body)
    s, body = api.call("POST", f"/api/v1/patients/{pid}/discharge", T["DOCTOR"], {})
    step("discharge from IPD", s, body)

    print("\n== Appointment (saga: slot reservation + provisional invoice)")
    s, body = api.call("POST", "/api/v1/appointments", T["PATIENT"], {
        "patientId": pid, "doctorId": doctor_id, "slot": f"{day}T10:30:00", "type": "OPD",
        "reason": "Chest pain", "durationMinutes": 15, "insuranceClaimed": False})
    appt = step("patient books appointment", s, body) or {}
    appt_id = appt.get("appointmentId")
    s, body = api.call("POST", "/api/v1/appointments", T["RECEPTIONIST"], {"patientId": pid, "type": "NOT_A_TYPE"})
    step("malformed request returns 400, not 500", s, body, expect=400)
    s, body = api.call("POST", "/api/v1/appointments", T["RECEPTIONIST"], {
        "patientId": pid, "doctorId": doctor_id, "slot": f"{day}T10:30:00", "type": "OPD",
        "reason": "Double booking", "durationMinutes": 15, "insuranceClaimed": False})
    step("double-booking same slot rejected", s, body, expect=(400, 409))
    s, body = api.call("POST", "/api/v1/appointments", T["PATIENT"], {
        "patientId": pid + 1000000, "doctorId": doctor_id, "slot": f"{day}T09:00:00", "type": "OPD",
        "reason": "Booking for someone else", "durationMinutes": 15, "insuranceClaimed": False})
    step("patient cannot book for another patient", s, body, expect=403)
    s, body = api.call("GET", f"/api/v1/appointments/{appt_id}", T["PATIENT"])
    step("get appointment", s, body)
    s, body = api.call("GET", "/api/v1/appointments", T["PATIENT"])
    mine = step("patient lists own appointments", s, body) or {}
    check("patient sees only own appointments", mine.get("content") and all(
        a.get("patientId") == pid for a in mine["content"]), str(mine)[:200])
    s, body = api.call("PATCH", f"/api/v1/appointments/{appt_id}/reschedule", T["PATIENT"], {"newSlot": f"{day}T11:00:00"})
    step("reschedule appointment", s, body)
    s, body = api.call("PATCH", f"/api/v1/appointments/{appt_id}/status", T["RECEPTIONIST"], {"status": "CHECKED_IN"})
    step("check in", s, body)
    s, body = api.call("GET", f"/api/v1/appointments/queue?doctorId={doctor_id}", T["DOCTOR"])
    step("OPD queue", s, body)
    s, body = api.call("GET", f"/api/v1/appointments?patientId={pid}", T["RECEPTIONIST"])
    step("query appointments", s, body)
    s, body = api.call("GET", f"/api/v1/billing/invoices?patientId={pid}", T["ACCOUNTANT"])
    invs = step("saga created provisional invoice", s, body)
    inv_list = invs.get("content", invs) if isinstance(invs, dict) else invs
    check("provisional invoice exists for appointment", bool(inv_list), str(invs)[:200])

    print("\n== EMR")
    s, body = api.call("POST", "/api/v1/emr/records", T["DOCTOR"], {
        "patientId": pid, "doctorId": doctor_id, "appointmentId": appt_id, "recordType": "CONSULTATION",
        "chiefComplaint": "Chest pain", "notes": "Stable, follow-up advised", "finalise": True})
    rec = step("doctor creates medical record", s, body) or {}
    rid = rec.get("id")
    s, body = api.call("POST", "/api/v1/emr/vitals", T["NURSE"], {
        "patientId": pid, "recordId": rid, "bpSystolic": 120, "bpDiastolic": 80, "pulse": 72,
        "temperature": 36.8, "spo2": 98, "heightCm": 170, "weightKg": 68})
    step("nurse records vitals", s, body)
    s, body = api.call("GET", f"/api/v1/emr/records/{rid}", T["PATIENT"])
    step("patient reads own record", s, body)
    s, body = api.call("GET", f"/api/v1/emr/records/{rid}", T["PHARMACIST"])
    step("pharmacist denied medical record", s, body, expect=403)
    s, body = api.call("GET", f"/api/v1/emr/records/{rid}", T["ACCOUNTANT"])
    step("accountant denied medical record", s, body, expect=403)
    s, body = api.call("GET", f"/api/v1/emr/records/{rid}", T["NURSE"])
    step("nurse reads medical record", s, body)
    s, body = api.call("POST", f"/api/v1/emr/records/{rid}/amend", T["DOCTOR"],
                       {"newValue": "Updated diagnosis notes", "reason": "Lab correction"})
    step("amend record", s, body)
    s, body = api.call("GET", f"/api/v1/emr/patients/{pid}/history", T["DOCTOR"])
    step("clinical history", s, body)
    s, body = api.call("POST", "/api/v1/emr/prescriptions", T["DOCTOR"], {
        "recordId": rid, "patientId": pid, "doctorId": doctor_id, "items": [
            {"drugName": "Paracetamol", "dosage": "500mg", "frequency": "TID", "durationDays": 3, "instructions": "After food"}]})
    rx = step("doctor writes prescription", s, body) or {}
    rx_id = rx.get("id")
    s, body = api.call("GET", f"/api/v1/emr/prescriptions/{rx_id}", T["PHARMACIST"])
    step("pharmacist reads prescription", s, body)
    s, payload = api.call("GET", f"/api/v1/emr/patients/{pid}/summary?format=PDF", T["DOCTOR"], raw=True)
    RESULTS.append((s == 200 and len(payload) > 0, "export patient summary (PDF)", s))
    print(f"  [{'PASS' if s == 200 and payload else 'FAIL'}] {s} export patient summary (PDF) ({len(payload)} bytes)")

    print("\n== Lab")
    s, body = api.call("GET", "/api/v1/lab/catalogue", T["DOCTOR"])
    cat = step("test catalogue", s, body) or []
    cat = cat.get("content", cat) if isinstance(cat, dict) else cat
    test_ids = [t["id"] for t in cat[:2]]
    check("catalogue seeded", len(test_ids) >= 1, str(cat)[:200])
    s, body = api.call("POST", "/api/v1/lab/orders", T["DOCTOR"],
                       {"patientId": pid, "doctorId": doctor_id, "priority": "NORMAL", "testIds": test_ids})
    order = step("doctor orders lab tests", s, body) or {}
    oid = order.get("id")
    for st in ("COLLECTED", "PROCESSING"):
        s, body = api.call("PATCH", f"/api/v1/lab/orders/{oid}/status", T["LAB_TECH"], {"status": st})
        step(f"lab tech sets {st}", s, body)
    for item in order.get("items", []):
        s, body = api.call("POST", f"/api/v1/lab/orders/{oid}/results", T["LAB_TECH"], {
            "orderItemId": item["id"], "value": "13.5", "unit": "g/dL", "referenceRange": "13-17", "abnormalFlag": False})
        step(f"upload result for item {item['id']}", s, body)
    s, body = api.call("GET", f"/api/v1/lab/orders/{oid}", T["PATIENT"])
    o = step("patient reads lab order", s, body) or {}
    check("lab order COMPLETED after all results", o.get("status") == "COMPLETED", str(o.get("status")))

    print("\n== Pharmacy")
    s, body = api.call("POST", "/api/v1/pharmacy/drugs", T["PHARMACIST"], {
        "code": f"PARA{run_id}", "genericName": "Paracetamol", "brandName": "Crocin", "form": "TABLET",
        "strength": "500mg", "manufacturer": "GSK", "unitPrice": 2.5, "reorderLevel": 100})
    drug = step("pharmacist adds drug", s, body) or {}
    drug_id = drug.get("id")
    s, body = api.call("PATCH", f"/api/v1/pharmacy/stock/{drug_id}", T["PHARMACIST"],
                       {"batchNo": f"B{run_id}", "quantity": 105, "expiryDate": "2027-06-30"})
    step("add stock batch", s, body)
    s, body = api.call("GET", "/api/v1/pharmacy/drugs?search=Paracetamol", T["DOCTOR"])
    step("search drug catalogue", s, body)
    s, body = api.call("POST", "/api/v1/pharmacy/dispense", T["PHARMACIST"], {
        "prescriptionId": rx_id, "patientId": pid, "patientCategory": "GENERAL",
        "items": [{"drugId": drug_id, "quantity": 9}]})
    step("dispense prescription (drops below reorder level)", s, body)
    s, body = api.call("GET", "/api/v1/pharmacy/stock/low", T["PHARMACIST"])
    low = step("low-stock report", s, body) or []
    low = low.get("content", low) if isinstance(low, dict) else low
    check("dispensed drug appears in low-stock report", any(
        (d.get("drugId") or d.get("id")) == drug_id for d in low), str(low)[:300])

    print("\n== Billing")
    s, body = api.call("POST", "/api/v1/billing/invoices", T["ACCOUNTANT"], {
        "patientId": pid, "appointmentId": appt_id, "items": [
            {"description": "ECG", "category": "PROCEDURE", "quantity": 1, "unitPrice": 400.0, "taxRate": 5}]})
    inv = step("accountant generates invoice", s, body) or {}
    inv_id = inv.get("id")
    total = inv.get("totalAmount") or inv.get("total") or inv.get("netAmount")
    check("invoice total computed (400 + 5% tax = 420)", total is not None and abs(float(total) - 420.0) < 0.01, str(inv)[:300])
    s, body = api.call("GET", f"/api/v1/billing/invoices/{inv_id}", T["PATIENT"])
    step("patient views own invoice", s, body)
    s, body = api.call("POST", "/api/v1/billing/payments", T["ACCOUNTANT"],
                       {"invoiceId": inv_id, "method": "UPI", "amount": float(total or 420), "reference": "john@okhdfcbank"})
    pay = step("record UPI payment", s, body) or {}
    s, body = api.call("GET", f"/api/v1/billing/invoices/{inv_id}", T["ACCOUNTANT"])
    paid = step("re-read invoice", s, body) or {}
    check("invoice PAID after full payment", paid.get("status") == "PAID", str(paid.get("status")))
    s, body = api.call("POST", "/api/v1/billing/refunds", T["ACCOUNTANT"],
                       {"paymentId": pay.get("paymentId"), "amount": 100.0, "reason": "Service not availed"})
    step("issue partial refund", s, body)
    today = dt.date.today()
    s, payload = api.call("GET", f"/api/v1/billing/reports/revenue?from={today.replace(day=1)}&to={today + dt.timedelta(days=30)}&format=CSV",
                          T["ACCOUNTANT"], raw=True)
    RESULTS.append((s == 200, "revenue report (CSV)", s))
    print(f"  [{'PASS' if s == 200 else 'FAIL'}] {s} revenue report (CSV) ({len(payload)} bytes)")

    print("\n== Appointment cancel (separate booking)")
    s, body = api.call("POST", "/api/v1/appointments", T["RECEPTIONIST"], {
        "patientId": pid, "doctorId": doctor_id, "slot": f"{day}T12:00:00", "type": "OPD",
        "reason": "To be cancelled", "durationMinutes": 15, "insuranceClaimed": False})
    a2 = step("receptionist books second appointment", s, body) or {}
    s, body = api.call("PATCH", f"/api/v1/appointments/{a2.get("appointmentId")}/cancel", T["PATIENT"], {"reason": "Patient request"})
    step("patient cancels", s, body)

    print("\n== Notifications (async via RabbitMQ)")
    patient_uid = users["PATIENT"].get("id")
    entries = []
    for _ in range(15):
        s, body = api.call("GET", f"/api/v1/notifications?userId={patient_uid}", T["ADMIN"])
        data = body.get("data") if isinstance(body, dict) else None
        entries = data.get("content", data) if isinstance(data, dict) else (data or [])
        if len(entries) >= 2:
            break
        time.sleep(2)
    step("query delivery log", s, body)
    check("event-driven notifications delivered to patient", len(entries) >= 1, f"{len(entries)} entries")
    print(f"         ({len(entries)} notifications: {sorted({e.get('templateCode') or e.get('eventType') or '?' for e in entries})})")
    s, body = api.call("POST", "/api/v1/notifications/send", T["ADMIN"], {
        "userId": patient_uid, "channel": "EMAIL", "templateCode": "APPOINTMENT_CONFIRMED",
        "variables": {"tokenNumber": "OPD-1-001", "slot": f"{day}T10:30:00"}})
    step("admin dispatches notification", s, body)
    s, body = api.call("PUT", "/api/v1/notifications/preferences", T["PATIENT"], {"channel": "SMS", "enabled": False})
    step("patient updates channel preferences", s, body)

    print("\n== Token lifecycle")
    s, body = api.call("POST", "/api/v1/auth/login",
                       body={"usernameOrEmail": f"patient_{run_id}@hms.local", "password": PASSWORD})
    t = step("login", s, body) or {}
    s, body = api.call("POST", "/api/v1/auth/refresh", body={"refreshToken": t.get("refreshToken")})
    step("refresh token", s, body)
    s, body = api.call("POST", "/api/v1/auth/logout", t.get("accessToken"), {"refreshToken": t.get("refreshToken")})
    step("logout", s, body)
    s, body = api.call("POST", "/api/v1/auth/refresh", body={"refreshToken": t.get("refreshToken")})
    step("refresh after logout rejected", s, body, expect=(400, 401))
    return {"pid": pid, "doctor_id": doctor_id, "appt_id": appt_id, "rid": rid, "rx_id": rx_id,
            "oid": oid, "inv_id": inv_id}


def run_web(web, run_id, ids):
    print("\n== Web UI")

    def session(role):
        opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()))

        def go(path, form=None):
            data = urllib.parse.urlencode(form).encode() if form is not None else None
            try:
                with opener.open(urllib.request.Request(web + path, data=data), timeout=30) as r:
                    return r.status, r.geturl(), r.read().decode(errors="replace")
            except urllib.error.HTTPError as e:
                return e.code, web + path, e.read().decode(errors="replace")

        if role:
            go("/login", {"usernameOrEmail": f"{role.lower()}_{run_id}@hms.local", "password": PASSWORD})
        return go

    anon = session(None)
    s, url, _ = anon("/dashboard")
    check("unauthenticated dashboard redirects to login", "/login" in url, url)
    s, url, html = anon("/login", {"usernameOrEmail": f"admin_{run_id}@hms.local", "password": "wrong-password"})
    check("bad web login stays on login page", "/login" in url or "Invalid" in html, url)

    i = ids
    pages = {
        "ADMIN": ["/dashboard", "/patients", f"/patients/{i['pid']}", "/patients/new", "/doctors",
                  "/doctors/new", f"/doctors/{i['doctor_id']}", f"/doctors/{i['doctor_id']}/schedule",
                  "/appointments", "/lab/catalogue", "/pharmacy/drugs", "/pharmacy/stock/low",
                  "/billing/invoices", "/billing/reports/revenue", "/notifications", "/notifications/preferences"],
        "RECEPTIONIST": ["/appointments/book", f"/appointments/{i['appt_id']}",
                         f"/appointments/queue?doctorId={i['doctor_id']}", "/billing/invoices/new"],
        "DOCTOR": ["/emr/records/new", f"/emr/records/{i['rid']}", f"/emr/patients/{i['pid']}/history",
                   "/emr/prescriptions/new", f"/emr/prescriptions/{i['rx_id']}", "/emr/vitals/new",
                   "/lab/orders/new", f"/lab/orders?id={i['oid']}"],
        "PHARMACIST": ["/pharmacy/drugs/new", "/pharmacy/dispense"],
        "ACCOUNTANT": [f"/billing/invoices/{i['inv_id']}", "/billing/payments/new"],
        "PATIENT": ["/dashboard", "/appointments", "/billing/invoices"],
    }
    for role, paths in pages.items():
        go = session(role)
        s, url, _ = go("/dashboard")
        check(f"web login as {role}", s == 200 and "/login" not in url, f"{s} {url}")
        for p in paths:
            s, url, html = go(p)
            bad = s != 200 or "/login" in url or "Whitelabel" in html
            check(f"web {role} GET {p}", not bad, f"{s} {url} {re.sub(r'\s+', ' ', html)[:300] if bad else ''}")
    go = session("PATIENT")
    s, url, html = go("/dashboard")
    check("web patient dashboard shows their own linked profile", f'href="/patients/{i["pid"]}"' in html, f"{s} {url}")
    go = session("PHARMACIST")
    s, url, _ = go("/notifications")
    check("web PHARMACIST denied /notifications", s == 403, f"{s} {url}")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--gateway", default="http://localhost:8080")
    ap.add_argument("--web", default="http://localhost:8090")
    ap.add_argument("--skip-web", action="store_true")
    ap.add_argument("--admin-email", default="admin@hms.local", help="bootstrap admin (HMS_ADMIN_EMAIL)")
    ap.add_argument("--admin-password", default="ChangeMe!Admin123", help="bootstrap admin (HMS_ADMIN_PASSWORD)")
    args = ap.parse_args()
    run_id = str(int(time.time()))[-7:]
    print(f"HMS e2e smoke run {run_id} against {args.gateway}")
    ids = run_api(Api(args.gateway), run_id, args.admin_email, args.admin_password)
    if not args.skip_web:
        run_web(args.web.rstrip("/"), run_id, ids)
    failed = [r for r in RESULTS if not r[0]]
    print(f"\n{len(RESULTS) - len(failed)}/{len(RESULTS)} checks passed")
    for _, name, status in failed:
        print(f"  FAILED: {name} ({status})")
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
