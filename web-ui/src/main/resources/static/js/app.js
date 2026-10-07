/**
 * Generic "add another row" helper for the dynamic multi-item forms (doctor schedule slots,
 * prescription items, invoice items, dispense items, EMR diagnoses). Each row template is a
 * <template> element; clicking the button clones it into the rows container.
 */
function addRow(rowsId, templateId) {
    const rows = document.getElementById(rowsId);
    const template = document.getElementById(templateId);
    const clone = template.content.cloneNode(true);
    rows.appendChild(clone);
}

function removeRow(button) {
    const row = button.closest('.dyn-row');
    if (row) row.remove();
}
