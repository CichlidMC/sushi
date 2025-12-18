package fish.cichlidmc.sushi.api.model;

import fish.cichlidmc.sushi.api.attach.AttachmentMap;

/// An object that may have arbitrary data attached to it.
/// This is useful for sharing data across contexts, such as between transformers.
public interface HasAttachments {
	AttachmentMap attachments();
}
