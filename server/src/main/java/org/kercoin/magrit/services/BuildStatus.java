package org.kercoin.magrit.services;

public enum BuildStatus {
	RUNNING('R'),
	ERROR('E'),
	OK('O'),
	INTERRUPTED('I'),
	LOCAL('L'),
	NEW('N'),
	UNKNOWN('?');
	
	private char code;
	
	private BuildStatus(char code) { this.code = code; }
	
	public char getCode() {
		return code;
	}
}
