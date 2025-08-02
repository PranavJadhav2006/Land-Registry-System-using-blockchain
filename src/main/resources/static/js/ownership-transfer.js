document.addEventListener('DOMContentLoaded', function() {
    // Initialize transfers table
    loadTransfers();
    
    // Form submission handlers
    document.getElementById('initiateTransferForm')?.addEventListener('submit', async function(e) {
        e.preventDefault();
        const formData = new FormData(this);
        const transferData = Object.fromEntries(formData.entries());

        try {
            const response = await fetch('/api/ownership-transfers/initiate', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(transferData)
            });

            const result = await response.json();

            if (response.ok) {
                showAlert('Transfer initiated successfully!', 'success');
                document.getElementById('initiateTransferResponse').textContent = 
                    `Transfer initiated successfully!\n\n` + 
                    `Transaction Hash: ${result.transactionHash}`;
                loadTransfers();
                this.reset();
            } else {
                showAlert(result.message || 'Transfer initiation failed.', 'danger');
            }
        } catch (error) {
            showAlert('An error occurred while initiating the transfer.', 'danger');
            console.error('Error initiating transfer:', error);
        }
    });
    
    // View transfer details button handler
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('view-transfer-btn')) {
            const surveyNumber = e.target.getAttribute('data-survey-number');
            viewTransferDetails(surveyNumber);
        }
    });
});

async function loadTransfers(page = 1, pageSize = 10, filters = {}) {
    try {
        const response = await fetch('/api/ownership-transfers/all');
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const transfers = await response.json();
        const totalTransfers = transfers.length;
    
    // Update UI
    const tableBody = document.getElementById('transferTableBody');
    if (tableBody) {
        tableBody.innerHTML = transfers.map(transfer => `
            <tr>
                <td>${transfer.id}</td>
                <td>${transfer.surveyNumber}</td>
                <td>${transfer.fromOwner}</td>
                <td>${transfer.toOwner}</td>
                <td>${formatDate(transfer.initiatedDate)}</td>
                <td><span class="badge bg-${transfer.status === 'Completed' ? 'success' : transfer.status === 'Pending' ? 'warning' : 'danger'}">${transfer.status}</span></td>
                <td>
                    <button class="btn btn-sm btn-outline-primary view-transfer-btn" data-survey-number="${transfer.surveyNumber}">
                        <i class="fas fa-eye"></i> View
                    </button>
                </td>
            </tr>
        `).join('');
        
        document.getElementById('transferCount').textContent = 
            `Showing ${1 + (page-1)*pageSize}-${Math.min(page*pageSize, totalTransfers)} of ${totalTransfers} transfers`;
        
        // Update pagination
        const paginationElement = document.querySelector('#transferTabsContent .pagination');
        if (paginationElement) {
            updatePagination(paginationElement, page, Math.ceil(totalTransfers / pageSize), (newPage) => {
                loadTransfers(newPage, pageSize, filters);
            });
        }
    }
}

async function viewTransferDetails(surveyNumber) {
    try {
        const response = await fetch(`/api/ownership-transfers/status/${surveyNumber}`);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const transferDetails = await response.json();
    
    // Show modal with transfer details
    const modal = new bootstrap.Modal(document.getElementById('transferDetailsModal'));
    const modalBody = document.getElementById('transferDetailsContent');
    
    modalBody.innerHTML = `
        <div class="row">
            <div class="col-md-6">
                <h5>Transfer Information</h5>
                <ul class="list-group list-group-flush mb-3">
                    <li class="list-group-item d-flex justify-content-between">
                        <span>Transfer ID:</span>
                        <strong>${transferDetails.id}</strong>
                    </li>
                    <li class="list-group-item d-flex justify-content-between">
                        <span>Survey Number:</span>
                        <strong>${transferDetails.surveyNumber}</strong>
                    </li>
                    <li class="list-group-item d-flex justify-content-between">
                        <span>Status:</span>
                        <span class="badge bg-${transferDetails.status === 'Completed' ? 'success' : transferDetails.status === 'Pending' ? 'warning' : 'danger'}">
                            ${transferDetails.status}
                        </span>
                    </li>
                    <li class="list-group-item d-flex justify-content-between">
                        <span>Initiated Date:</span>
                        <strong>${formatDateTime(transferDetails.initiatedDate)}</strong>
                    </li>
                </ul>
            </div>
            <div class="col-md-6">
                <h5>Ownership Details</h5>
                <ul class="list-group list-group-flush">
                    <li class="list-group-item d-flex justify-content-between">
                        <span>Current Owner:</span>
                        <strong>${transferDetails.fromOwner}</strong>
                    </li>
                    <li class="list-group-item d-flex justify-content-between">
                        <span>New Owner:</span>
                        <strong>${transferDetails.toOwner}</strong>
                    </li>
                    <li class="list-group-item d-flex justify-content-between">
                        <span>Transaction Hash:</span>
                        <strong class="text-truncate" style="max-width: 150px;">${transferDetails.txHash}</strong>
                    </li>
                    <li class="list-group-item d-flex justify-content-between">
                        <span>Transfer Reason:</span>
                        <strong>${transferDetails.reason}</strong>
                    </li>
                </ul>
            </div>
        </div>
        <div class="mt-3">
            <h5>Supporting Documents</h5>
            <div class="list-group">
                ${transferDetails.documents.map(doc => `
                    <a href="#" class="list-group-item list-group-item-action">
                        <i class="fas fa-file-alt me-2"></i> ${doc.name} (uploaded ${formatDate(doc.date)})
                    </a>
                `).join('')}
            </div>
        </div>
    `;
    
    modal.show();
}