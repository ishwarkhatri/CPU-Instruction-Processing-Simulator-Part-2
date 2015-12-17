package edu.binghamton.my.model;

import static edu.binghamton.my.common.Constants.SPACE;

public class Instruction {

	private int robSlotId;

	private int pc;

	private InstructionType opCode;

	private boolean isValid;

	private FunctionalUnitType fuType;

	private boolean isSrc1Ready;

	private String src1RegName;

	private int src1Value = -1;

	private boolean isSrc2Ready;

	private String src2RegName;

	private int src2Value = -1;

	private boolean isDestReady;

	private String destRegName;

	private int destinationValue = -1;

	private int multiplyLatencyCount;

	private int noOfSources;

	private String stringRepresentation;

	private boolean isToBeSqaushed;

	private boolean branchPredictionTaken;

	public boolean isBranchPredictionTaken() {
		return branchPredictionTaken;
	}

	public void setBranchPredictionTaken(boolean branchPrediction) {
		this.branchPredictionTaken = branchPrediction;
	}

	public boolean isToBeSqaushed() {
		return isToBeSqaushed;
	}

	public void setToBeSqaushed(boolean isToBeSqaushed) {
		this.isToBeSqaushed = isToBeSqaushed;
	}

	public int getNoOfSources() {
		return noOfSources;
	}

	public void setNoOfSources(int noOfSources) {
		this.noOfSources = noOfSources;
	}

	public int getSrc1Value() {
		return src1Value;
	}

	public void setSrc1Value(int src1Value) {
		this.src1Value = src1Value;
	}

	public int getSrc2Value() {
		return src2Value;
	}

	public void setSrc2Value(int src2Value) {
		this.src2Value = src2Value;
	}

	public int getDestinationValue() {
		return destinationValue;
	}

	public void setDestinationValue(int destinationValue) {
		this.destinationValue = destinationValue;
	}

	public int getRobSlotId() {
		return robSlotId;
	}

	public void setRobSlotId(int robSlotId) {
		this.robSlotId = robSlotId;
	}

	public int getPc() {
		return pc;
	}

	public void setPc(int pc) {
		this.pc = pc;
	}

	public InstructionType getOpCode() {
		return opCode;
	}

	public void setOpCode(InstructionType opCode) {
		this.opCode = opCode;
	}

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

	public boolean isSrc1Ready() {
		return isSrc1Ready;
	}

	public void setSrc1Ready(boolean isSrc1Ready) {
		this.isSrc1Ready = isSrc1Ready;
	}

	public String getSrc1RegName() {
		return src1RegName;
	}

	public void setSrc1RegName(String src1) {
		this.src1RegName = src1;
	}

	public boolean isSrc2Ready() {
		return isSrc2Ready;
	}

	public void setSrc2Ready(boolean isSrc2Ready) {
		this.isSrc2Ready = isSrc2Ready;
	}

	public String getSrc2RegName() {
		return src2RegName;
	}

	public void setSrc2RegName(String src2) {
		this.src2RegName = src2;
	}

	public String getDestRegName() {
		return destRegName;
	}

	public void setDestRegName(String destination) {
		this.destRegName = destination;
	}

	public boolean isDestReady() {
		return isDestReady;
	}

	public void setDestReady(boolean isDestReady) {
		this.isDestReady = isDestReady;
	}

	public String getZeroOperandInstruction() {
		return this.opCode.getValue();
	}

	public String getOneOperandInstruction() {
		return this.opCode.getValue() + SPACE + this.destRegName;
	}

	public String getTwoOperandInstruction() {
		return this.opCode.getValue() + SPACE + this.destRegName + SPACE + this.src1RegName;
	}

	public String getThreeOperandInstruction() {
		return this.opCode.getValue() + SPACE + this.destRegName + SPACE + this.src1RegName + SPACE + this.src2RegName;
	}

	public int getMultiplyLatencyCount() {
		return multiplyLatencyCount;
	}

	public void setMultiplyLatencyCount(int count) {
		this.multiplyLatencyCount = count;
	}

	public void setStringRepresentation(String string) {
		this.stringRepresentation = string;
	}

	public String getStringRepresentation() {
		return stringRepresentation;
	}
}
