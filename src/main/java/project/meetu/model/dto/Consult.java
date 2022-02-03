package project.meetu.model.dto;

public class Consult {
	private int id;
	private String startDate;
	private String endDate;
	private String reason;
	private int type;
	private int state;
	private ServiceUser profUser;
	private ServiceUser stuUser;
	private String cancelMsg;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public ServiceUser getProfUser() {
		return profUser;
	}
	public void setProfUser(ServiceUser profUser) {
		this.profUser = profUser;
	}
	public ServiceUser getStuUser() {
		return stuUser;
	}
	public void setStuUser(ServiceUser stuUser) {
		this.stuUser = stuUser;
	}
	public String getCancelMsg() {
		return cancelMsg;
	}
	public void setCancelMsg(String cancelMsg) {
		this.cancelMsg = cancelMsg;
	}
	
}