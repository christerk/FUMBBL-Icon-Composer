package com.fumbbl.iconcomposer.dto.fumbbl;

import java.util.Collection;
import java.util.HashSet;

import com.fumbbl.iconcomposer.model.types.Roster;
import com.fumbbl.iconcomposer.model.types.Ruleset;

public class DtoRuleset {
	public int id;
	public String value;
	
	public Collection<DtoRoster> rosters;
	
	public Ruleset toRuleset() {
		Ruleset r = new Ruleset();
		r.id = id;
		r.name = value;
		
		Collection<Roster> list = new HashSet<Roster>();
		if (rosters != null) {
			rosters.forEach(roster -> list.add(roster.toRoster()));
		}
		r.rosters = list;
		return r;
	}
}
