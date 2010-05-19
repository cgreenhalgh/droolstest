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
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + tickCount;
		result = prime * result + (int) (time ^ (time >>> 32));
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SystemTime other = (SystemTime) obj;
		if (tickCount != other.tickCount)
			return false;
		if (time != other.time)
			return false;
		return true;
	}
	
}
