package com.fumbbl.iconcomposer.dto.fumbbl;

import java.util.Collection;
import java.util.HashSet;

import com.fumbbl.iconcomposer.model.types.Position;
import com.fumbbl.iconcomposer.model.types.Roster;

public class DtoRoster {
	public int id;
	public String value;
	
	public Collection<DtoPosition> positions;

	public Roster toRoster() {
		Roster r = new Roster();
		r.id = id;
		r.name = value;

		Collection<Position> positions = new HashSet<>();
		if (this.positions != null) {
			this.positions.forEach(p -> positions.add(p.toPosition()));
		}
		r.positions = positions;
		
		return r;
	}
}
