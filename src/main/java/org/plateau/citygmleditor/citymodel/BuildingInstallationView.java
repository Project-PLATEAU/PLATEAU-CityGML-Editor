package org.plateau.citygmleditor.citymodel;

import java.util.List;
import java.util.ArrayList;
import javafx.scene.shape.MeshView;
import org.citygml4j.model.citygml.building.BuildingInstallation;
import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.plateau.citygmleditor.citygmleditor.TransformManipulator;
import org.plateau.citygmleditor.citymodel.geometry.GeometryView;
import org.plateau.citygmleditor.citymodel.geometry.ILODSolidView;
import org.plateau.citygmleditor.citymodel.geometry.PolygonView;
import org.plateau.citygmleditor.utils3d.polygonmesh.TexCoordBuffer;
import org.plateau.citygmleditor.utils3d.polygonmesh.VertexBuffer;
import org.plateau.citygmleditor.world.World;

public class BuildingInstallationView extends MeshView implements ILODSolidView {
    private BuildingInstallation gmlObject;
    private GeometryView geometryView;
    private VertexBuffer vertexBuffer = new VertexBuffer();
    private TexCoordBuffer texCoordBuffer = new TexCoordBuffer();
    private TransformManipulator transformManipulator = new TransformManipulator(this);

    public BuildingInstallationView(BuildingInstallation gmlObject, VertexBuffer vertexBuffer, TexCoordBuffer texCoordBuffer) {
        this.gmlObject = gmlObject;
        this.vertexBuffer = vertexBuffer;
        this.texCoordBuffer = texCoordBuffer;
    }

    public BuildingInstallation getGMLObject() {
        return this.gmlObject;
    }

    public void setGeometryView(GeometryView geometry) {
        this.geometryView = geometry;
    }

    GeometryView getGeometryView() {
        return this.geometryView;
    }
    
    @Override
    public AbstractSolid getAbstractSolid() {
        return null;
    }

    @Override
    public ArrayList<PolygonView> getPolygons() {
        return geometryView.getPolygons();
    }

    @Override
    public VertexBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    public void setVertexBuffer(VertexBuffer vertexBuffer) {
        this.vertexBuffer = vertexBuffer;
    }

    @Override
    public TexCoordBuffer getTexCoordBuffer() {
        return texCoordBuffer;
    }

    @Override
    public TransformManipulator getTransformManipulator() {
        return transformManipulator;
    }

    @Override
    public void refrectGML() {
        for (var polygon : getPolygons()) {
            var coordinates = polygon.getExteriorRing().getOriginCoords();// .getOriginal().getPosList().toList3d();
            polygon.getExteriorRing().getOriginal().getPosList()
                    .setValue(transformManipulator.unprojectTransforms(coordinates));

            for (var interiorRing : polygon.getInteriorRings()) {
                var coordinatesInteriorRing = interiorRing.getOriginCoords();// .getOriginal().getPosList().toList3d();
                polygon.getExteriorRing().getOriginal().getPosList().setValue(
                        transformManipulator.unprojectTransforms(coordinatesInteriorRing));
            }
        }
        var vertexBuffer = new VertexBuffer();
        var vertices = transformManipulator.unprojectVertexTransforms(getVertexBuffer().getVertices());
        for (var vertex : vertices) {
            vertexBuffer.addVertex(vertex);
        }
        setVertexBuffer(vertexBuffer);
    }
    
    public List<String> getTexturePaths() {
        var cityModelView = World.getActiveInstance().getCityModel();
        var ret = new ArrayList<String>();
        for (var polygon : getPolygons()) {
            if (polygon.getSurfaceData() == null)
                continue;
            var parameterizedTexture = (org.citygml4j.model.citygml.appearance.ParameterizedTexture)polygon.getSurfaceData().getOriginal();
            var imageRelativePath = java.nio.file.Paths.get(parameterizedTexture.getImageURI());
            var imageAbsolutePath = java.nio.file.Paths.get(cityModelView.getGmlPath()).getParent().resolve(imageRelativePath);
            if(!ret.contains(imageAbsolutePath.toString()))
                ret.add(imageAbsolutePath.toString());
        }
        return ret;
    }
}
