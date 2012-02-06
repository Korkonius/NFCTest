/**
 * 
 */
package com.korkonius.skysstickets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * This is the primary class for visualizing the collected ticket data.
 * Using OpenGL ES 2.0 this class visualizes the data passed to it as
 * 
 * 
 * @author Eirik Eggesbø Ottesen
 */
public class GLTicketDataView extends GLSurfaceView {
	
	/**
	 * Private renderer class tasked with rendering the surface in the view,
	 * this function receives the data passed to the surface view and creates
	 * the texture and geometry needed to represent it.
	 * 
	 * @author Eirik Eggesbø Ottesen
	 */
	class GLTicketDataViewRenderer implements GLSurfaceView.Renderer {

		// Buffer for test geometry
		private FloatBuffer testGeometry;
		
		// FIXME Temporary vertex and fragment shaders
		private final String vertexShaderCode =
				"uniform mat4 uMVPMatrix;  \n" +
				"attribute vec4 vPosition; \n" +
				"void main(){              \n" +
				" gl_Position = uMVPMatrix * vPosition; \n" +
				"}                         \n";
		
		private final String fragmentShaderCode = 
				"precision mediump float;  \n" +
				"void main(){              \n" +
				" gl_FragColor = vec4 (0.63671875, 0.76953125, 0.22265625, 1.0); \n" +
				"}                         \n";
		
		// Handles for shader and geometry position
		private int mProgram;
		private int maPositionHandle;
		
		// Transformation matrices
		private int MVPMatrixHandle;
		private float[] MVPMatrix = new float[16];
		private float[] MMatrix = new float[16];
		private float[] VMatrix = new float[16];
		private float[] ProjMatrix = new float[16];
		
		// Public members to affect rotation
		public float Angle;
		
		private void initTestShapes() {
			
			// Float coordinates
			float triangleCoords[] = {
					
					// X, Y, Z
					-0.5f, -0.25f, 0,
					0.5f, -0.25f, 0,
					0.0f, 0.559016994f, 0
			};
			
			// Create and store vertex buffer
			ByteBuffer vbb = ByteBuffer.allocateDirect(triangleCoords.length * 4);
			vbb.order(ByteOrder.nativeOrder());
			
			// Store in private variable
			testGeometry = vbb.asFloatBuffer();
			testGeometry.put(triangleCoords);
			testGeometry.position(0);
		}
		
		private int loadShader(int type, String shaderCode) {
			int shader = GLES20.glCreateShader(type);
			GLES20.glShaderSource(shader, shaderCode);
			GLES20.glCompileShader(shader);
			
			return shader;
		}
		
		public void onDrawFrame(GL10 gl) {
	        // Redraw background color
	        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
	        
	        // Attach shader
	        GLES20.glUseProgram(mProgram);
	        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 12, this.testGeometry);
	        GLES20.glEnableVertexAttribArray(maPositionHandle);
	        
	        // Apply rotation
	        Matrix.setRotateM(MMatrix, 0, this.Angle, 0.0f, 0.0f, 1.0f);
	        Matrix.multiplyMM(MVPMatrix, 0, VMatrix, 0, MMatrix, 0);
	        
	        // Apply model view matrix
	        Matrix.multiplyMM(MVPMatrix, 0, ProjMatrix, 0, MVPMatrix, 0);
	        GLES20.glUniformMatrix4fv(MVPMatrixHandle, 1, false, MVPMatrix, 0);
	        
	        // Draw geometry
	        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
		}

		public void onSurfaceChanged(GL10 gl, int width, int height) {
			GLES20.glViewport(0, 0, width, height);
			
			// Apply new projection matrix to compansate for orientation
			float ratio = (float) width / height;
			Matrix.frustumM(ProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
			MVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
			Matrix.setLookAtM(VMatrix, 0, 0, 0, -3, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
		}

		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			// Set background color
			GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
			initTestShapes();
			
			// Load and attach shader
			int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
			int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
			
			mProgram = GLES20.glCreateProgram();
			GLES20.glAttachShader(mProgram, vertexShader);
			GLES20.glAttachShader(mProgram, fragmentShader);
			GLES20.glLinkProgram(mProgram);
			
			maPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
		}
		
	}
	
	// Parameters for animation
	private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
	private GLTicketDataViewRenderer renderer;
	private float previousX;
	private float previousY;
	
	private void initializaInternals() {
		// Create an OpenGL ES 2.0 context.
		setEGLContextClientVersion(2);
		
		// Set renderer to the implementation contained within the class
		this.renderer = new GLTicketDataViewRenderer();
		setRenderer(this.renderer);
		
		// Render view only when changed
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}
	
	public GLTicketDataView(Context context) {
		super(context);
		
		this.initializaInternals();
	}
	
	public GLTicketDataView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		this.initializaInternals();
	}
	
	public boolean onTouchEvent(MotionEvent e) {
		
		float x = e.getX();
		float y = e.getY();
		
		switch(e.getAction()) {
		case MotionEvent.ACTION_MOVE:
			float dx = x - this.previousX;
			float dy = y - this.previousY;
			
			// Reverse rotation above mid-line
			if(y > getHeight() / 2) {
				dx = dx * -1;
			}
			
			// Reverse rotation left of mid-line
			if(x < getWidth() / 2) {
				dy = dy * -1;
			}
			
			this.renderer.Angle += (dx+dy) * TOUCH_SCALE_FACTOR;
			requestRender();
		}
		
		previousX = x;
		previousY = y;
		
		return true;
	}

}
