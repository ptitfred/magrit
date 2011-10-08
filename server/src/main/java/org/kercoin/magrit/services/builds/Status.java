package org.kercoin.magrit.services.builds;

public enum Status {
	RUNNING('R'),
	PENDING('P'),
	ERROR('E'),
	OK('O'),
	INTERRUPTED('I'),
	LOCAL('L'),
	NEW('N'),
	UNKNOWN('?');
	
	private char code;
	
	private Status(char code) { this.code = code; }
	
	public char getCode() {
		return code;
	}
}
