const BASE_URL = 'http://localhost:8000/api';

export const api = {
    getLands: async () => {
        const response = await fetch(`${BASE_URL}/land`);
        const data = await response.json();
        if (!response.ok || !data.success) {
            throw new Error(data.message || `HTTP error! status: ${response.status}`);
        }
        return data;
    },
    registerLand: async (landData) => {
        const response = await fetch(`${BASE_URL}/land/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(landData)
        });
        const data = await response.json();
        if (!response.ok || !data.success) {
            throw new Error(data.message || `HTTP error! status: ${response.status}`);
        }
        return data;
    }
};