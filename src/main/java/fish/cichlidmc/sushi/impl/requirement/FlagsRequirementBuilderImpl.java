package fish.cichlidmc.sushi.impl.requirement;

import fish.cichlidmc.sushi.api.requirement.Requirement;
import fish.cichlidmc.sushi.api.requirement.builtin.FlagsRequirement;
import org.glavo.classfile.AccessFlag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class FlagsRequirementBuilderImpl implements FlagsRequirement.Builder {
	private final String reason;
	private final Set<FlagsRequirement.Entry> entries;
	private final List<Requirement> chained;

	public FlagsRequirementBuilderImpl(String reason) {
		this.reason = reason;
		this.entries = new HashSet<>();
		this.chained = new ArrayList<>();
	}

	@Override
	public FlagsRequirement.Builder require(AccessFlag flag) throws IllegalArgumentException {
		if (!this.entries.add(FlagsRequirement.Entry.require(flag))) {
			throw new IllegalArgumentException("Duplicate flag: " + flag);
		}

		return this;
	}

	@Override
	public FlagsRequirement.Builder forbid(AccessFlag flag) throws IllegalArgumentException {
		if (!this.entries.add(FlagsRequirement.Entry.forbid(flag))) {
			throw new IllegalArgumentException("Duplicate flag: " + flag);
		}

		return this;
	}

	@Override
	public FlagsRequirement.Builder chain(Requirement requirement) {
		this.chained.add(requirement);
		return this;
	}

	@Override
	public FlagsRequirement build() {
		return new FlagsRequirement(this.reason, Set.copyOf(this.entries), List.copyOf(this.chained));
	}
}
