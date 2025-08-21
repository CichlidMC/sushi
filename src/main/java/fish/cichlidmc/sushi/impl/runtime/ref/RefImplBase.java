package fish.cichlidmc.sushi.impl.runtime.ref;

abstract class RefImplBase {
	private boolean discarded;

	public void discard() {
		this.discarded = true;
	}

	protected void checkDiscarded() {
		if (this.discarded) {
			throw new IllegalStateException("Ref has already been discarded");
		}
	}
}
