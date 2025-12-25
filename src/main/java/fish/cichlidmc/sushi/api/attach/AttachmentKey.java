package fish.cichlidmc.sushi.api.attach;

/// A key identifying an attachment that may be held by [AttachmentMap]s.
/// Has identity semantics. One instance should be created and held onto for attachment access.
public record AttachmentKey<T>() {
	@Override
	public boolean equals(Object obj) {
		return obj == this;
	}

	@Override
	public int hashCode() {
		return System.identityHashCode(this);
	}
}
