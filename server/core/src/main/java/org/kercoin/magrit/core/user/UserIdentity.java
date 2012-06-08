/*
Copyright 2011-2012 Frederic Menou and others referred in AUTHORS file.

This file is part of Magrit.

Magrit is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of
the License, or (at your option) any later version.

Magrit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public
License along with Magrit.
If not, see <http://www.gnu.org/licenses/>.
*/
package org.kercoin.magrit.core.user;

public class UserIdentity {
	private final String email;
	private final String name;
	private final String toString;

	public UserIdentity(String email, String name) {
		super();
		this.email = email;
		this.name = name;
		this.toString = String.format("\"%s\" <%s>", name, email);
	}
	
	public String getEmail() {
		return email;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return toString;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((toString == null) ? 0 : toString.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserIdentity other = (UserIdentity) obj;
		if (toString == null) {
			if (other.toString != null)
				return false;
		} else if (!toString.equals(other.toString))
			return false;
		return true;
	}
	
	
}
