package org.plateau.citygmleditor.citymodel.factory;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.appearance.AppearanceMember;
import org.citygml4j.model.citygml.appearance.ParameterizedTexture;
import org.citygml4j.model.citygml.appearance.TexCoordList;
import org.plateau.citygmleditor.citymodel.AppearanceView;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.citymodel.SurfaceDataView;

import java.nio.file.Paths;

public class AppearanceFactory extends CityGMLFactory {

    protected AppearanceFactory(CityModelView target) {
        super(target);
    }

    public AppearanceView createAppearance(AppearanceMember gmlObject) {
        var appearance = new AppearanceView(gmlObject.getAppearance());

        var surfaceDataMembers = gmlObject.getAppearance().getSurfaceDataMember();
        for (var surfaceData : surfaceDataMembers) {
            if (surfaceData.getSurfaceData().getCityGMLClass() == CityGMLClass.PARAMETERIZED_TEXTURE) {
                appearance.getSurfaceData().add(createParameterizedTexture((ParameterizedTexture) surfaceData.getSurfaceData()));
            }

            if (surfaceData.getSurfaceData().getCityGMLClass() == CityGMLClass.X3D_MATERIAL) {
                throw new UnsupportedOperationException();
            }
        }

        return appearance;
    }

    private SurfaceDataView createParameterizedTexture(ParameterizedTexture parameterizedTexture) {
        var imageRelativePath = Paths.get(parameterizedTexture.getImageURI());
        var imageAbsolutePath = Paths.get(getTarget().getGmlPath()).getParent().resolve(imageRelativePath);

        Image image = null;
        URI u = null;
        try {
            u = new URI(imageAbsolutePath.toString());
            boolean isWeb = "http".equalsIgnoreCase(u.getScheme())
                || "https".equalsIgnoreCase(u.getScheme());
            if(isWeb) {
                image = new Image(imageAbsolutePath.toString(), 256, 256, true, false);
            } else {
                image = new Image(new FileInputStream(new File(imageAbsolutePath.toString())), 256, 256, true, false);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // サイズが大きすぎるテクスチャがあると極端にパフォーマンス落ちるためリサイズ
        // TODO: 元の解像度で表示
        var material = new PhongMaterial();
        material.setDiffuseMap(image);

        var surfaceData = new SurfaceDataView(parameterizedTexture);
        surfaceData.setMaterial(material);

        for (var target : parameterizedTexture.getTarget()) {
            if (target.getTextureParameterization().getCityGMLClass() == CityGMLClass.TEX_COORD_LIST) {
                var textureParameter = (TexCoordList) target.getTextureParameterization();
                for (var texCoords :textureParameter.getTextureCoordinates()) {
                    var coords = new float[texCoords.getValue().size()];
                    for (int i = 0; i < coords.length; ++i) {
                        coords[i] = texCoords.getValue().get(i).floatValue();
                    }

                    surfaceData.getTextureCoordinatesByRing().put(texCoords.getRing(), coords);
                }
            }
        }

        return surfaceData;
    }
}
