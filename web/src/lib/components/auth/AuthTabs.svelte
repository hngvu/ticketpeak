<script lang="ts">
	let {
		activeTab = $bindable('login')
	}: {
		activeTab?: 'login' | 'register';
	} = $props();
</script>

<div class="mb-6 flex w-full border-b border-hairline">
	<button
		type="button"
		onclick={() => (activeTab = 'login')}
		class="flex-1 cursor-pointer border-b-2 pb-3 text-center text-sm font-bold transition duration-150
			{activeTab === 'login'
			? 'border-primary text-primary'
			: 'border-transparent text-mute hover:text-body'}"
	>
		Sign In
	</button>
	<button
		type="button"
		onclick={() => (activeTab = 'register')}
		class="flex-1 cursor-pointer border-b-2 pb-3 text-center text-sm font-bold transition duration-150
			{activeTab === 'register'
			? 'border-primary text-primary'
			: 'border-transparent text-mute hover:text-body'}"
	>
		Create Account
	</button>
</div>
