package entities;

/**
* The class contains information of the region on interest.
* 
* @author  Yaoguang Zhong
* @version 1.1
* @since   08-02-2016
*/
public class ROIFrame {

	// the type of frame:
	// RED: the red frame
	// BLUE: the blue frame
	// SINGLE: the frame with a single color
	public enum FrameType { RED, BLUE, SINGLE };

	private int frameId = 0; // the frame id for the avi video file
	private FrameType frameType = null;
	// is the frame needed to be record for the larva in the csv file?
	private Boolean needRecord = false;
	// is the frame needed to be tracked for the larva?
	private Boolean needTrack = false; 
	
	public ROIFrame(int frameId)
	{
		this.frameId = frameId;
	}
	
	public int getFrameId() {
		return frameId;
	}
	public void setFrameId(int frameId) {
		this.frameId = frameId;
	}
	public FrameType getFrameType() {
		return frameType;
	}
	public void setFrameType(FrameType frameType) {
		this.frameType = frameType;
	}
	public Boolean getNeedRecord() {
		return needRecord;
	}
	public void setNeedRecord(Boolean needRecord) {
		this.needRecord = needRecord;
	}
	public Boolean getNeedTrack() {
		return needTrack;
	}
	public void setNeedTrack(Boolean needTrack) {
		this.needTrack = needTrack;
	}
	
	
}
