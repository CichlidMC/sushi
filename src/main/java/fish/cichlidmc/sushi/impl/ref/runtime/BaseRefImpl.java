package fish.cichlidmc.sushi.impl.ref.runtime;

public abstract class BaseRefImpl {
	private boolean discarded;

	// references are injected at runtime
	@SuppressWarnings("unused")
	public void discard() {
		this.discarded = true;
	}

	protected void checkDiscarded() {
		if (this.discarded) {
			throw new IllegalStateException("Ref has already been discarded");
		}
	}
}
