package com.gl.tangoclick;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.LinearInterpolator;

import com.google.atap.tangoservice.TangoPoseData;
import com.projecttango.tangosupport.TangoSupport;

import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.EllipticalOrbitAnimation3D;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.StreamingTexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.ScreenQuad;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.util.ObjectColorPicker;
import org.rajawali3d.util.OnObjectPickedListener;

import javax.microedition.khronos.opengles.GL10;

/**
 * Renderer that implements a basic augmented reality scene using Rajawali.
 * It creates a scene with a background quad taking the whole screen, where the color camera is
 * rendered and a sphere with the texture of the earth floats ahead of the start position of
 * the Tango device.
 */
public class AugmentedRealityRenderer extends Renderer implements OnObjectPickedListener {
    private static final String TAG = AugmentedRealityRenderer.class.getSimpleName();

    private float[] textureCoords0 = new float[]{0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, 0.0F};

    // Rajawali texture used to render the Tango color camera.
    private ATexture mTangoCameraTexture;

    // Keeps track of whether the scene camera has been configured.
    private boolean mSceneCameraConfigured;

    private ScreenQuad mBackgroundQuad;
    private ObjectColorPicker mPicker;
    private ObjectClickListener mListener;

    public AugmentedRealityRenderer(Context context) {
        super(context);
    }

    @Override
    protected void initScene() {
        // Create a quad covering the whole background and assign a texture to it where the
        // Tango color camera contents will be rendered.
        Material tangoCameraMaterial = new Material();
        tangoCameraMaterial.setColorInfluence(0);

        if (mBackgroundQuad == null) {
            mBackgroundQuad = new ScreenQuad();
            mBackgroundQuad.getGeometry().setTextureCoords(textureCoords0);
        }
        // We need to use Rajawali's {@code StreamingTexture} since it sets up the texture
        // for GL_TEXTURE_EXTERNAL_OES rendering.
        mTangoCameraTexture =
                new StreamingTexture("camera", (StreamingTexture.ISurfaceListener) null);
        try {
            tangoCameraMaterial.addTexture(mTangoCameraTexture);
            mBackgroundQuad.setMaterial(tangoCameraMaterial);
        } catch (ATexture.TextureException e) {
            Log.e(TAG, "Exception creating texture for RGB camera contents", e);
        }
        getCurrentScene().addChildAt(mBackgroundQuad, 0);

        // Add a directional light in an arbitrary direction.
        DirectionalLight light = new DirectionalLight(1, 0.2, -1);
        light.setColor(1, 1, 1);
        light.setPower(0.8f);
        light.setPosition(3, 2, 4);
        getCurrentScene().addLight(light);

        CustomLoaderObj parser = new CustomLoaderObj(mContext.getResources(),
                mTextureManager, R.raw.arrow48);
        try {
            parser.parse();
        } catch (ParsingException e) {
            e.printStackTrace();
        }
        mPicker = new ObjectColorPicker(this);
        mPicker.setOnObjectPickedListener(this);
        Object3D object3D = parser.getParsedObject();
        object3D.setScale(0.01f, 0.01f, 0.03f);
        object3D.setPosition(0,0,-3);
        getCurrentScene().addChild(object3D);
        mPicker.registerObject(object3D);
        Animation3D animSphere = new RotateOnAxisAnimation(Vector3.Axis.X, 0, 270);
        animSphere.setInterpolator(new LinearInterpolator());
        animSphere.setDurationMilliseconds(200);
        //   animSphere.setRepeatMode(Animation.RepeatMode.INFINITE);
        animSphere.setTransformable3D(object3D);
        getCurrentScene().registerAnimation(animSphere);
        animSphere.play();

/*        // Create sphere with earth texture and place it in space 3m forward from the origin.
        Material earthMaterial = new Material();
        try {
            Texture t = new Texture("earth", R.drawable.earth);
            earthMaterial.addTexture(t);
        } catch (ATexture.TextureException e) {
            Log.e(TAG, "Exception generating earth texture", e);
        }
        earthMaterial.setColorInfluence(0);
        earthMaterial.enableLighting(true);
        earthMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());
        Object3D earth = new Sphere(0.4f, 20, 20);
        earth.setMaterial(earthMaterial);
        earth.setPosition(0, 0, -3);
        getCurrentScene().addChild(earth);

        // Rotate around its Y axis
        Animation3D animEarth = new RotateOnAxisAnimation(Vector3.Axis.Y, 0, -360);
        animEarth.setInterpolator(new LinearInterpolator());
        animEarth.setDurationMilliseconds(60000);
        animEarth.setRepeatMode(Animation.RepeatMode.INFINITE);
        animEarth.setTransformable3D(earth);
        getCurrentScene().registerAnimation(animEarth);
        animEarth.play();

        // Create sphere with moon texture.
        Material moonMaterial = new Material();
        try {
            Texture t = new Texture("moon", R.drawable.moon);
            moonMaterial.addTexture(t);
        } catch (ATexture.TextureException e) {
            Log.e(TAG, "Exception generating moon texture", e);
        }
        moonMaterial.setColorInfluence(0);
        moonMaterial.enableLighting(true);
        moonMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());
        Object3D moon = new Sphere(0.1f, 20, 20);
        moon.setMaterial(moonMaterial);
        moon.setPosition(0, 0, -1);
        getCurrentScene().addChild(moon);

        // Rotate the moon around its Y axis.
        Animation3D animMoon = new RotateOnAxisAnimation(Vector3.Axis.Y, 0, -360);
        animMoon.setInterpolator(new LinearInterpolator());
        animMoon.setDurationMilliseconds(60000);
        animMoon.setRepeatMode(Animation.RepeatMode.INFINITE);
        animMoon.setTransformable3D(moon);
        getCurrentScene().registerAnimation(animMoon);
        animMoon.play();

        // Make the moon orbit around the earth. The first two parameters are the focal point and
        // periapsis of the orbit.
        Animation3D translationMoon = new EllipticalOrbitAnimation3D(new Vector3(0, 0, -5),
                new Vector3(0, 0, -1), Vector3.getAxisVector(Vector3.Axis.Y), 0,
                360, EllipticalOrbitAnimation3D.OrbitDirection.COUNTERCLOCKWISE);
        translationMoon.setDurationMilliseconds(60000);
        translationMoon.setRepeatMode(Animation.RepeatMode.INFINITE);
        translationMoon.setTransformable3D(moon);
        getCurrentScene().registerAnimation(translationMoon);
        translationMoon.play();*/
    }

    /**
     * Update background texture's UV coordinates when device orientation is changed (i.e., change
     * between landscape and portrait mode).
     * This must be run in the OpenGL thread.
     */
    public void updateColorCameraTextureUvGlThread(int rotation) {
        if (mBackgroundQuad == null) {
            mBackgroundQuad = new ScreenQuad();
        }

        float[] textureCoords =
                TangoSupport.getVideoOverlayUVBasedOnDisplayRotation(textureCoords0, rotation);
        mBackgroundQuad.getGeometry().setTextureCoords(textureCoords, true);
        mBackgroundQuad.getGeometry().reload();
    }

    /**
     * Update the scene camera based on the provided pose in Tango start of service frame.
     * The camera pose should match the pose of the camera color at the time of the last rendered
     * RGB frame, which can be retrieved with this.getTimestamp();
     * <p/>
     * NOTE: This must be called from the OpenGL render thread; it is not thread-safe.
     */
    public void updateRenderCameraPose(TangoPoseData cameraPose) {
        float[] rotation = cameraPose.getRotationAsFloats();
        float[] translation = cameraPose.getTranslationAsFloats();
        Quaternion quaternion = new Quaternion(rotation[3], rotation[0], rotation[1], rotation[2]);
        // Conjugating the Quaternion is needed because Rajawali uses left-handed convention for
        // quaternions.
        getCurrentCamera().setRotation(quaternion.conjugate());
        getCurrentCamera().setPosition(translation[0], translation[1], translation[2]);
    }

    /**
     * It returns the ID currently assigned to the texture where the Tango color camera contents
     * should be rendered.
     * NOTE: This must be called from the OpenGL render thread; it is not thread-safe.
     */
    public int getTextureId() {
        return mTangoCameraTexture == null ? -1 : mTangoCameraTexture.getTextureId();
    }

    /**
     * We need to override this method to mark the camera for re-configuration (set proper
     * projection matrix) since it will be reset by Rajawali on surface changes.
     */
    @Override
    public void onRenderSurfaceSizeChanged(GL10 gl, int width, int height) {
        super.onRenderSurfaceSizeChanged(gl, width, height);
        mSceneCameraConfigured = false;
    }

    public boolean isSceneCameraConfigured() {
        return mSceneCameraConfigured;
    }

    /**
     * Sets the projection matrix for the scene camera to match the parameters of the color camera,
     * provided by the {@code TangoCameraIntrinsics}.
     */
    public void setProjectionMatrix(float[] matrixFloats) {
        getCurrentCamera().setProjectionMatrix(new Matrix4(matrixFloats));
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset,
                                 float xOffsetStep, float yOffsetStep,
                                 int xPixelOffset, int yPixelOffset) {
    }

    @Override
    public void onTouchEvent(MotionEvent event) {

    }

    @Override
    public void onObjectPicked(@NonNull Object3D object) {
        Log.i("Object Picked", "Object Picked2");
        mListener.objectClick("Object Picked");
    }

    @Override
    public void onNoObjectPicked() {
        mListener.objectClick("No Object Pick");
    }


    public void getObjectAt(float x, float y) {
        if (mPicker != null) {
            Log.i("Object Picked", "X" + x + " y" + y);
            mPicker.getObjectAt(x, y);
        }
    }

    public void setListener(ObjectClickListener listener) {
        mListener = listener;
    }
}
