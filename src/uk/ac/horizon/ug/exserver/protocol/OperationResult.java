/**
 * 
 */
package uk.ac.horizon.ug.exserver.protocol;


/**
 * @author cmg
 *
 */
public class OperationResult {
	/** status */
	protected OperationStatus status = OperationStatus.UNKNOWN;
	/** request */
	protected RawFactHolder holder;
	/** return fact handle */
	protected String handle;
	/**
	 * 
	 */
	public OperationResult() {
		super();
	}
	/**
	 * @return the status
	 */
	public OperationStatus getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(OperationStatus status) {
		this.status = status;
	}
	/**
	 * @return the holder
	 */
	public RawFactHolder getHolder() {
		return holder;
	}
	/**
	 * @param holder the holder to set
	 */
	public void setHolder(RawFactHolder holder) {
		this.holder = holder;
	}
	/**
	 * @return the handle
	 */
	public String getHandle() {
		return handle;
	}
	/**
	 * @param handle the handle to set
	 */
	public void setHandle(String handle) {
		this.handle = handle;
	}
	
}
