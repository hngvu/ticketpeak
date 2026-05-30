/**
 * Request a presigned upload URL from the SvelteKit proxy and upload a file directly to MinIO/S3.
 * Returns the stable public download URL and the unique file key.
 */
export async function uploadFileDirect(
	file: File
): Promise<{ downloadUrl: string; fileKey: string }> {
	const res = await fetch('/api/storage', {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify({
			fileName: file.name,
			contentType: file.type
		})
	});

	if (!res.ok) {
		const json = await res.json().catch(() => ({}));
		throw new Error(json.message || 'Failed to request upload signature.');
	}

	const json = await res.json();
	const { uploadUrl, downloadUrl, fileKey } = json.data;

	const uploadRes = await fetch(uploadUrl, {
		method: 'PUT',
		headers: {
			'Content-Type': file.type
		},
		body: file
	});

	if (!uploadRes.ok) {
		throw new Error('Direct binary upload to MinIO/S3 storage failed.');
	}

	return { downloadUrl, fileKey };
}
