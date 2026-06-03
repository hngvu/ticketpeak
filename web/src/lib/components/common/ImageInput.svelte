<script lang="ts">
	import { uploadFileDirect } from '$lib/storage';

	let { value = $bindable(''), name = '' } = $props<{
		value?: string;
		name?: string;
	}>();

	let isUploading = $state(false);
	let uploadError = $state('');
	let isUrlModalOpen = $state(false);
	let tempUrl = $state('');

	let fileInput = $state<HTMLInputElement | null>(null);

	function triggerFileInput() {
		fileInput?.click();
	}

	async function handleFileChange(event: Event) {
		const target = event.target as HTMLInputElement;
		if (!target.files || target.files.length === 0) return;
		const file = target.files[0];

		isUploading = true;
		uploadError = '';
		try {
			const result = await uploadFileDirect(file);
			value = result.downloadUrl;
		} catch (err: unknown) {
			uploadError = err instanceof Error ? err.message : String(err);
		} finally {
			isUploading = false;
			target.value = '';
		}
	}

	function openUrlModal() {
		tempUrl = value || 'http://';
		isUrlModalOpen = true;
	}

	function saveUrl() {
		if (tempUrl.trim() && tempUrl.trim() !== 'http://') {
			value = tempUrl.trim();
		} else {
			value = '';
		}
		isUrlModalOpen = false;
	}

	function closeUrlModal() {
		isUrlModalOpen = false;
	}
</script>

<!-- Hidden Inputs for HTML Form Submission -->
<input type="hidden {name ? '' : 'hidden'}" {name} {value} />

<div class="w-full">
	{#if isUploading}
		<div
			class="flex min-h-[140px] flex-col items-center justify-center gap-2.5 rounded-xl border border-[#E4E4E7] bg-[#FAFAFA]"
		>
			<svg class="h-6 w-6 animate-spin text-[#71717A]" fill="none" viewBox="0 0 24 24">
				<circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"
				></circle>
				<path
					class="opacity-75"
					fill="currentColor"
					d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
				></path>
			</svg>
			<span class="text-xs font-semibold text-[#71717A]">Uploading image...</span>
		</div>
	{:else if value}
		<div
			class="group relative flex max-h-[220px] min-h-[140px] items-center justify-center overflow-hidden rounded-xl border border-[#E4E4E7] bg-[#FAFAFA]"
		>
			<img src={value} alt="Preview" class="max-h-[210px] max-w-full object-contain p-2" />
			<!-- Hover Overlay with Actions -->
			<div
				class="absolute inset-0 flex flex-col items-center justify-center gap-2 bg-black/60 opacity-0 transition-opacity duration-200 group-hover:opacity-100"
			>
				<button
					type="button"
					onclick={triggerFileInput}
					class="cursor-pointer rounded-md bg-white px-3.5 py-1.5 text-xs font-bold text-slate-800 transition hover:bg-slate-100"
				>
					Upload another image
				</button>
				<button
					type="button"
					onclick={openUrlModal}
					class="cursor-pointer rounded-md bg-white px-3.5 py-1.5 text-xs font-bold text-slate-800 transition hover:bg-slate-100"
				>
					Change URL
				</button>
				<button
					type="button"
					onclick={() => (value = '')}
					class="cursor-pointer rounded-md bg-rose-600 px-3.5 py-1.5 text-xs font-bold text-white transition hover:bg-rose-700"
				>
					Remove image
				</button>
			</div>
		</div>
	{:else}
		<!-- Dashed Box Matching Design 1 -->
		<div
			class="flex min-h-[140px] flex-col items-center justify-center rounded-xl border-2 border-dashed border-[#E4E4E7] bg-blue-50/5 p-6 text-center transition hover:border-[#026CDF] hover:bg-blue-50/10"
		>
			<div class="flex flex-col items-center gap-2 select-none">
				<button
					type="button"
					onclick={triggerFileInput}
					class="cursor-pointer text-sm font-bold text-[#026CDF] hover:underline"
				>
					Add image
				</button>
				<span class="text-xs font-medium text-[#71717A]">or</span>
				<button
					type="button"
					onclick={openUrlModal}
					class="cursor-pointer text-sm font-bold text-[#026CDF] hover:underline"
				>
					Add from URL
				</button>
			</div>
		</div>
	{/if}

	{#if uploadError}
		<p class="mt-1 text-[10px] font-semibold text-rose-600">⚠️ {uploadError}</p>
	{/if}

	<!-- Hidden Input File Trigger -->
	<input
		type="file"
		accept="image/*"
		class="hidden"
		bind:this={fileInput}
		onchange={handleFileChange}
	/>
</div>

<!-- ======================== ADD FROM URL DIALOG MODAL ======================== -->
{#if isUrlModalOpen}
	<div class="fixed inset-0 z-[100] flex items-center justify-center bg-black/40 p-4">
		<div
			class="w-full max-w-[480px] overflow-hidden rounded-lg border border-[#E4E4E7] bg-white shadow-xl"
		>
			<!-- Header -->
			<div class="flex items-center justify-between border-b border-[#E4E4E7] px-5 py-4">
				<h4 class="text-sm font-bold text-slate-800">Add from URL</h4>
				<button
					type="button"
					onclick={closeUrlModal}
					class="cursor-pointer rounded-full p-1 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600"
				>
					<svg
						class="h-4.5 w-4.5"
						fill="none"
						viewBox="0 0 24 24"
						stroke="currentColor"
						stroke-width="2.5"
					>
						<path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
					</svg>
				</button>
			</div>

			<!-- Body -->
			<div class="px-5 py-5">
				<div class="space-y-2">
					<label for="img-url-input" class="block text-xs font-semibold text-[#71717A]"
						>Enter image or YouTube URL</label
					>
					<input
						type="text"
						id="img-url-input"
						bind:value={tempUrl}
						class="w-full rounded-md border border-[#E4E4E7] bg-[#FAFAFA] px-3.5 py-2.5 font-sans text-xs text-[#111111] outline-none focus:border-[#71717A] focus:bg-white"
					/>
				</div>
			</div>

			<!-- Footer -->
			<div
				class="flex items-center justify-end gap-3 border-t border-[#E4E4E7] bg-slate-50/50 px-5 py-3.5"
			>
				<button
					type="button"
					onclick={closeUrlModal}
					class="cursor-pointer rounded-md bg-[#EDEDED] px-4 py-2 text-xs font-bold text-slate-700 transition hover:bg-slate-200"
				>
					Cancel
				</button>
				<button
					type="button"
					onclick={saveUrl}
					class="cursor-pointer rounded-md bg-[#026CDF] px-4 py-2 text-xs font-bold text-white transition hover:bg-[#0156b3]"
				>
					Save
				</button>
			</div>
		</div>
	</div>
{/if}
