package com.turt2live.dumbplots.plot;

public class CornerPlotCorner {

	private CornerType type;
	private PlotCorner corner;
	private boolean valid = false;
	private ChunkLoc location;

	public CornerPlotCorner(String corner, String world) {
		// Format Example: A=10,4
		// A=10,4 means "corner A is at chunk (10,4)"
		String[] parts1 = corner.split("=");
		int x, z;
		String cornerType;
		if (parts1.length == 2) {
			cornerType = parts1[0];
			if (cornerType.length() > 1) {
				throw new IllegalArgumentException("Corner is not in the format C=X,Z (Error type 2)");
			} else {
				String coords = parts1[1];
				String[] parts2 = coords.split(",");
				if (parts2.length == 2) {
					try {
						x = Integer.parseInt(parts2[0]);
						z = Integer.parseInt(parts2[1]);
					} catch(NumberFormatException e) {
						throw new IllegalArgumentException("Corner is not in the format C=X,Z (Error type 4)");
					}
				} else {
					throw new IllegalArgumentException("Corner is not in the format C=X,Z (Error type 3)");
				}
			}
		} else {
			throw new IllegalArgumentException("Corner is not in the format C=X,Z (Error type 1)");
		}
		this.valid = true;
		this.corner = new PlotCorner(x, z, world);
		this.type = CornerType.valueOf(cornerType);
		this.location = new ChunkLoc(x, z);
	}

	public CornerPlotCorner(CornerType type, PlotCorner corner) {
		this.type = type;
		this.corner = corner;
	}

	public ChunkLoc getLocation() {
		return location;
	}

	public boolean isValid() {
		return valid;
	}

	public CornerType getType() {
		return type;
	}

	public PlotCorner getCorner() {
		return corner;
	}

	@Override
	public String toString() {
		return type.name() + "=" + location.toString();
	}

}
