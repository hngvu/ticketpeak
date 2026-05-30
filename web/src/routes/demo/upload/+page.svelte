<script lang="ts">
	import { uploadFileDirect } from '$lib/storage';
	import { fade } from 'svelte/transition';

	let fileInput = $state<HTMLInputElement | null>(null);
	let dragging = $state(false);
	let uploading = $state(false);
	let errorMsg = $state<string | null>(null);
	let successMsg = $state<string | null>(null);
	let uploadResult = $state<{ downloadUrl: string; fileKey: string } | null>(null);
	let uploadedImageName = $state('');
	let uploadedImageSize = $state('');

	function formatBytes(bytes: number, decimals = 2) {
		if (!bytes) return '0 Bytes';
		const k = 1024;
		const dm = decimals < 0 ? 0 : decimals;
		const sizes = ['Bytes', 'KB', 'MB', 'GB'];
		const i = Math.floor(Math.log(bytes) / Math.log(k));
		return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
	}

	async function handleUpload(file: File) {
		if (!file) return;

		errorMsg = null;
		successMsg = null;
		uploadResult = null;
		uploading = true;

		uploadedImageName = file.name;
		uploadedImageSize = formatBytes(file.size);

		try {
			const result = await uploadFileDirect(file);
			uploadResult = result;
			successMsg = 'File uploaded directly to storage successfully!';
		} catch (err) {
			const error = err as Error;
			console.error(error);
			errorMsg = error.message || 'Failed to upload file. Please ensure you are logged in.';
		} finally {
			uploading = false;
		}
	}

	function onFileChange(e: Event) {
		const target = e.target as HTMLInputElement;
		if (target.files && target.files.length > 0) {
			handleUpload(target.files[0]);
		}
	}

	function onDrop(e: DragEvent) {
		e.preventDefault();
		dragging = false;
		if (e.dataTransfer?.files && e.dataTransfer.files.length > 0) {
			handleUpload(e.dataTransfer.files[0]);
		}
	}

	function onDragOver(e: DragEvent) {
		e.preventDefault();
		dragging = true;
	}

	function onDragLeave() {
		dragging = false;
	}

	function triggerSelect() {
		fileInput?.click();
	}

	let copied = $state(false);
	function copyUrl() {
		if (!uploadResult?.downloadUrl) return;
		navigator.clipboard.writeText(uploadResult.downloadUrl);
		copied = true;
		setTimeout(() => (copied = false), 2000);
	}
</script>

<svelte:head>
	<title>S3 / MinIO Upload Tester | Ticketpeak</title>
</svelte:head>

<div class="flex min-h-screen flex-col items-center bg-canvas-soft px-4 py-16 select-none sm:px-6">
	<div
		class="w-full max-w-2xl space-y-8 rounded-2xl border border-hairline bg-canvas p-6 shadow-sm sm:p-8"
	>
		<!-- Header -->
		<div class="space-y-2 border-b border-hairline pb-6 text-center">
			<span
				class="inline-flex items-center gap-1.5 rounded-full border border-primary/20 bg-primary/10 px-3 py-1 text-xs font-bold tracking-wide text-primary uppercase"
			>
				<span class="h-1.5 w-1.5 rounded-full bg-primary"></span>
				Object Storage
			</span>
			<h1 class="font-sans text-2xl font-extrabold tracking-tight text-ink sm:text-3xl">
				Direct-to-Storage Upload
			</h1>
			<p class="mx-auto max-w-md text-sm font-medium text-mute">
				Upload images directly to S3 / MinIO via secure backend presigned URLs, avoiding app-server
				overhead.
			</p>
		</div>

		<!-- File Drop Zone -->
		<!-- svelte-ignore a11y_click_events_have_key_events -->
		<!-- svelte-ignore a11y_no_static_element_interactions -->
		<div
			onclick={triggerSelect}
			ondragover={onDragOver}
			ondragleave={onDragLeave}
			ondrop={onDrop}
			class="relative flex min-h-[220px] cursor-pointer flex-col items-center justify-center rounded-xl border-2 border-dashed p-8 text-center transition-all duration-300 select-none
				{dragging
				? 'scale-[1.01] border-primary bg-primary/5'
				: 'border-hairline hover:border-hairline-strong hover:bg-canvas-soft/30'}
				{uploading ? 'pointer-events-none opacity-60' : ''}"
		>
			<input
				type="file"
				bind:this={fileInput}
				onchange={onFileChange}
				accept="image/jpeg,image/jpg,image/png,image/webp,image/gif"
				class="hidden"
			/>

			{#if uploading}
				<div class="space-y-4">
					<svg
						class="mx-auto h-12 w-12 animate-spin text-primary"
						xmlns="http://www.w3.org/2000/svg"
						fill="none"
						viewBox="0 0 24 24"
					>
						<circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"
						></circle>
						<path
							class="opacity-75"
							fill="currentColor"
							d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
						></path>
					</svg>
					<div class="space-y-1">
						<p class="font-sans text-sm font-bold text-ink">Uploading file directly...</p>
						<p class="font-mono text-xs text-mute">{uploadedImageName} ({uploadedImageSize})</p>
					</div>
				</div>
			{:else}
				<div class="space-y-4">
					<div
						class="mx-auto flex h-12 w-12 items-center justify-center rounded-full border border-hairline bg-canvas-soft text-mute"
					>
						<svg
							class="h-6 w-6"
							fill="none"
							stroke="currentColor"
							stroke-width="2"
							viewBox="0 0 24 24"
							xmlns="http://www.w3.org/2000/svg"
						>
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								d="M12 16.5V9.75m0 0l3 3m-3-3l-3 3M6.75 19.5a4.5 4.5 0 01-1.41-8.775 5.25 5.25 0 0110.233-2.33 3 3 0 013.758 3.848A3.752 3.752 0 0118 19.5H6.75z"
							/>
						</svg>
					</div>
					<div class="space-y-1.5">
						<p class="font-sans text-sm font-bold text-ink">
							<span class="text-primary hover:underline">Click to upload</span> or drag and drop
						</p>
						<p class="font-sans text-xs text-mute">
							Only image types allowed: JPEG, PNG, WEBP, GIF (Max 10MB)
						</p>
					</div>
				</div>
			{/if}
		</div>

		<!-- Feedback Messages -->
		{#if errorMsg}
			<div
				transition:fade={{ duration: 150 }}
				class="flex items-start gap-3 rounded-lg border border-red-200 bg-red-50 p-4 text-xs leading-normal font-semibold text-red-800"
			>
				<svg
					class="mt-0.5 h-4.5 w-4.5 shrink-0 text-red-500"
					fill="none"
					stroke="currentColor"
					stroke-width="2.5"
					viewBox="0 0 24 24"
					xmlns="http://www.w3.org/2000/svg"
				>
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						d="M12 9v3.75m9-.75a9 9 0 11-18 0 9 9 0 0118 0zm-9 3.75h.008v.008H12v-.008z"
					/>
				</svg>
				<p>{errorMsg}</p>
			</div>
		{/if}

		{#if successMsg && uploadResult}
			<div transition:fade={{ duration: 150 }} class="space-y-6">
				<!-- Success alert -->
				<div
					class="flex items-start gap-3 rounded-lg border border-emerald-200 bg-emerald-50 p-4 text-xs leading-normal font-semibold text-emerald-800"
				>
					<svg
						class="mt-0.5 h-4.5 w-4.5 shrink-0 text-emerald-500"
						fill="none"
						stroke="currentColor"
						stroke-width="2.5"
						viewBox="0 0 24 24"
						xmlns="http://www.w3.org/2000/svg"
					>
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							d="M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
						/>
					</svg>
					<p>{successMsg}</p>
				</div>

				<!-- Preview & URL Card -->
				<div class="overflow-hidden rounded-xl border border-hairline bg-canvas-soft shadow-2xs">
					<!-- Preview box -->
					<div
						class="relative flex aspect-video items-center justify-center border-b border-hairline bg-black/5"
					>
						<img
							src={uploadResult.downloadUrl}
							alt="Uploaded preview"
							class="h-full w-full object-contain"
						/>
					</div>

					<!-- URL display -->
					<div class="space-y-3 p-4 font-sans">
						<div class="space-y-1">
							<span class="font-mono text-[10px] font-bold tracking-wider text-mute uppercase"
								>File Metadata</span
							>
							<p class="truncate text-xs font-bold text-ink">
								{uploadedImageName} · {uploadedImageSize}
							</p>
							<p class="truncate font-mono text-[10px] text-mute">Key: {uploadResult.fileKey}</p>
						</div>

						<div class="space-y-1.5">
							<span class="font-mono text-[10px] font-bold tracking-wider text-mute uppercase"
								>Download URL</span
							>
							<div class="flex gap-2">
								<input
									type="text"
									readonly
									value={uploadResult.downloadUrl}
									class="h-9 w-full truncate rounded border border-hairline bg-canvas px-3 text-xs text-mute focus:outline-none"
								/>
								<button
									type="button"
									onclick={copyUrl}
									class="h-9 shrink-0 cursor-pointer rounded px-4 text-xs font-bold shadow-2xs transition-colors select-none
										{copied
										? 'bg-emerald-600 text-white hover:bg-emerald-700'
										: 'bg-ink text-on-primary hover:bg-ink/90'}"
								>
									{copied ? 'Copied!' : 'Copy'}
								</button>
							</div>
						</div>
					</div>
				</div>
			</div>
		{/if}
	</div>
</div>
