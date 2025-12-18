package fish.cichlidmc.sushi.api.attach;

import fish.cichlidmc.sushi.impl.attach.AttachmentKeyImpl;

/// A key identifying an attachment that may be held by [AttachmentMap]s.
/// Has identity semantics. One instance should be created and held onto.
public sealed interface AttachmentKey<T> permits AttachmentKeyImpl {
	static <T> AttachmentKey<T> create() {
		return new AttachmentKeyImpl<>();
	}
}
