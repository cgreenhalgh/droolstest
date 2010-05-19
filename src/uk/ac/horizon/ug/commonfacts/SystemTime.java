/**
 * 
 */
package uk.ac.horizon.ug.commonfacts;

/**
 * @author cmg
 *
 */
public class SystemTime implements java.io.Serializable {
	/** system UNIX time milliseconds (java-styple), hopefully GMT */
	long time;
	/** tick (update) count */
	int tickCount;
	/**
	 * cons
	 */
	public SystemTime() {
		super();
	}
	/**
	 * @param time
	 * @param tickCount
	 */
	public SystemTime(long time, int tickCount) {
		super();
		this.time = time;
		this.tickCount = tickCount;
	}
	/**
	 * @return the time
	 */
	public long getTime() {
		return time;
	}
	/**
	 * @param time the time to set
	 */
	public void setTime(long time) {
		this.time = time;
	}
	/**
	 * @return the tickCount
	 */
	public int getTickCount() {
		return tickCount;
	}
	/**
	 * @param tickCount the tickCount to set
	 */
	public void setTickCount(int tickCount) {
		this.tickCount = tickCount;
	}
	
}
