package com.gl.tangoclick;

/**
 * Created by shilpi.das on 02-05-2017.
 */


import android.content.res.Resources;

import org.rajawali3d.Object3D;
import org.rajawali3d.loader.ALoader;
import org.rajawali3d.loader.IMeshLoader;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.materials.textures.TextureManager;
import org.rajawali3d.renderer.Renderer;

import java.io.File;

public abstract class MyAMeshLoader extends ALoader implements IMeshLoader {

    protected TextureManager mTextureManager;

    protected Object3D mRootObject;

    public MyAMeshLoader(File file) {
        super(file);
        mRootObject = new Object3D();
    }

    public MyAMeshLoader(String fileOnSDCard) {
        super(fileOnSDCard);
        mRootObject = new Object3D();
    }

    public MyAMeshLoader(Renderer renderer, String fileOnSDCard) {
        super(renderer, fileOnSDCard);
        mRootObject = new Object3D();
    }

    public MyAMeshLoader(Resources resources, TextureManager textureManager, int resourceId) {
        super(resources, resourceId);
        mTextureManager = textureManager;
        mRootObject = new Object3D();
    }

    public MyAMeshLoader(Renderer renderer, File file) {
        super(renderer, file);
        mRootObject = new Object3D();
    }

    public MyAMeshLoader parse() throws ParsingException {
        super.parse();
        return this;
    }

    public Object3D getParsedObject() {
        return mRootObject;
    }

    public class MaterialDef {

        public String name;
        public int ambientColor;
        public int diffuseColor;
        public int specularColor;
        public float specularCoefficient;
        public float alpha = 1f;
        public String ambientTexture;
        public String diffuseTexture;
        public String specularColorTexture;
        public String specularHighlightTexture;
        public String alphaTexture;
        public String bumpTexture;
    }
}

