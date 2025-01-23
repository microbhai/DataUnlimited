package akhil.DataUnlimited.model.parameter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GlobalVirtualFileParameters {

	private static final Logger lo = LogManager.getLogger(ParameterStore.class.getName());
	private VirtualFileParam vfp = new VirtualFileParam();

	public VirtualFileParam getVFP() {
		return vfp;
	}

	public void reset() {
		vfp = new VirtualFileParam();
	}

	public boolean setVFP(VirtualFileParam vfp) {
		if (vfp != null) {
			boolean overwrote = this.vfp.mergeVirtualFileParam(vfp);
			if (overwrote)
				lo.warn("WARNING: Some virtual files were overwritten. Check logs for details.");

			return true;
		} else {
			lo.error(
					"ERROR: Virtual File Param data could not be parsed successfully. Make sure it follows the right format of declaring virtual files. Use DMSV UI to check the format.");
			return false;
		}
	}

	public boolean deleteVF(String filename) {
		if (vfp != null) {
			if (vfp.hasVF(filename)) {
				vfp.remove(filename);
				return true;
			} else
				return false;
		} else
			return false;
	}

	private static GlobalVirtualFileParameters gvfp;

	private GlobalVirtualFileParameters() {
	}

	public static GlobalVirtualFileParameters getInstance() {
		if (gvfp == null)
			gvfp = new GlobalVirtualFileParameters();
		return gvfp;
	}

}
