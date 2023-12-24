package org.plateau.citygmleditor.citymodel;

import org.citygml4j.model.citygml.appearance.Appearance;

import java.util.ArrayList;

public class AppearanceView extends GMLObjectView<Appearance> {
    private ArrayList<SurfaceDataView> surfaceData = new ArrayList<>();

    public AppearanceView(org.citygml4j.model.citygml.appearance.Appearance original) {
        super(original);
    }

    public ArrayList<SurfaceDataView> getSurfaceData() {
        return surfaceData;
    }

    public void setSurfaceData(ArrayList<SurfaceDataView> surfaceData) {
        this.surfaceData = surfaceData;
    }
}
