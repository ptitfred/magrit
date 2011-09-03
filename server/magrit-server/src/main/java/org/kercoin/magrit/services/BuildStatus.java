package org.kercoin.magrit.services;

public enum BuildStatus {
	NEW('N'), CLEAN('C'), UNKNOWN('?');
	
	private char code;
	
	private BuildStatus(char code) { this.code = code; }
	
	public char getCode() {
		return code;
	}
}
