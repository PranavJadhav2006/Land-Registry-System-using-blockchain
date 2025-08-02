

document.addEventListener('DOMContentLoaded', function() {
    const uploadForm = document.getElementById('uploadDocumentForm');
    const uploadResponse = document.getElementById('uploadDocumentResponse');
    const documentTableBody = document.getElementById('documentTableBody');
    const filterBtn = document.getElementById('filterBtn');

    // Load documents from the server on page load
    if (document.getElementById('manage-tab')) {
        fetchDocuments();
    }

    if (uploadForm) {
        uploadForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            const formData = new FormData(this);

            uploadResponse.innerHTML = `<div class="alert alert-info">Uploading to IPFS...</div>`;

            try {
                const response = await fetch('http://localhost:8000/api/documents/upload', {
                    method: 'POST',
                    body: formData
                });

                if (response.ok) {
                    const newDoc = await response.json();
                    uploadResponse.innerHTML = `
                        <div class="alert alert-success">
                            File uploaded successfully!
                            <br>
                            <strong>IPFS Hash:</strong> ${newDoc.ipfsHash}
                        </div>`;
                    fetchDocuments(); // Refresh the documents table
                } else {
                    const errorText = await response.text();
                    uploadResponse.innerHTML = `<div class="alert alert-danger">Error uploading file: ${errorText}</div>`;
                }
            } catch (error) {
                uploadResponse.innerHTML = `<div class="alert alert-danger">Error uploading file: ${error.message}</div>`;
            }
        });
    }

    if (filterBtn) {
        filterBtn.addEventListener('click', function() {
            fetchDocuments();
        });
    }

    async function fetchDocuments() {
        const searchInput = document.getElementById('searchInput').value;
        const filterType = document.getElementById('filterType').value;

        try {
            const response = await fetch('http://localhost:8000/api/documents/all');
            if (response.ok) {
                let documents = await response.json();

                const filteredDocs = documents.filter(doc => {
                    const matchesSearch = doc.id.includes(searchInput) || doc.surveyNumber.includes(searchInput) || doc.description.includes(searchInput);
                    const matchesType = filterType ? doc.documentType === filterType : true;
                    return matchesSearch && matchesType;
                });

                renderDocuments(filteredDocs);
            } else {
                console.error('Failed to fetch documents');
            }
        } catch (error) {
            console.error('Error fetching documents:', error);
        }
    }

    function renderDocuments(documents) {
        documentTableBody.innerHTML = '';
        if (documents.length === 0) {
            documentTableBody.innerHTML = '<tr><td colspan="6" class="text-center">No documents found.</td></tr>';
            return;
        }

        documents.forEach(doc => {
            const row = `
                <tr>
                    <td>${doc.id}</td>
                    <td>${doc.documentType}</td>
                    <td>${doc.surveyNumber}</td>
                    <td>${new Date(doc.uploadedAt).toLocaleDateString()}</td>
                    <td>N/A</td> <!-- Size is not available in the backend response -->
                    <td>
                        <a href="https://ipfs.io/ipfs/${doc.ipfsHash}" target="_blank" class="btn btn-sm btn-primary"><i class="fas fa-eye"></i></a>
                        <button class="btn btn-sm btn-danger" onclick="deleteDocument('${doc.id}')"><i class="fas fa-trash"></i></button>
                    </td>
                </tr>
            `;
            documentTableBody.innerHTML += row;
        });
    }

    window.deleteDocument = async function(docId) {
        try {
            const response = await fetch(`http://localhost:8000/api/documents/${docId}`, {
                method: 'DELETE'
            });

            if (response.ok) {
                fetchDocuments(); // Refresh the documents table
            } else {
                console.error('Failed to delete document');
            }
        } catch (error) {
            console.error('Error deleting document:', error);
        }
    }
});

