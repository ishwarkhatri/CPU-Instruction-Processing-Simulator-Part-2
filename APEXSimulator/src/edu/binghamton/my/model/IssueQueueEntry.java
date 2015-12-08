package edu.binghamton.my.model;

public class IssueQueueEntry {

	private boolean isValid;

	private FunctionalUnitType fuType;

	private int literal;

	private boolean isSrc1Ready;

	private String src1;

	private boolean isSrc2Ready;

	private String src2;

	private String destination;

	public boolean isValid() {
		return isValid;
	}

	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}

	public FunctionalUnitType getFuType() {
		return fuType;
	}

	public void setFuType(FunctionalUnitType fuType) {
		this.fuType = fuType;
	}

	public int getLiteral() {
		return literal;
	}

	public void setLiteral(int literal) {
		this.literal = literal;
	}

	public boolean isSrc1Ready() {
		return isSrc1Ready;
	}

	public void setSrc1Ready(boolean isSrc1Ready) {
		this.isSrc1Ready = isSrc1Ready;
	}

	public String getSrc1() {
		return src1;
	}

	public void setSrc1(String src1) {
		this.src1 = src1;
	}

	public boolean isSrc2Ready() {
		return isSrc2Ready;
	}

	public void setSrc2Ready(boolean isSrc2Ready) {
		this.isSrc2Ready = isSrc2Ready;
	}

	public String getSrc2() {
		return src2;
	}

	public void setSrc2(String src2) {
		this.src2 = src2;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

}
