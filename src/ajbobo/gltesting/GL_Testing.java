package ajbobo.gltesting;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;

/** Main activity for the program */
public class GL_Testing extends Activity
{
	private GLSurfaceView glsurface;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		glsurface = new CustomGLSurface(this);
		setContentView(glsurface);
		glsurface.requestFocus();
		glsurface.setFocusableInTouchMode(true);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		glsurface.onResume();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		glsurface.onPause();
	}
}

/** A surface that draws the graphics and handles user input */
class CustomGLSurface extends GLSurfaceView
{
	private MyRenderer renderer;
	private float lastX, lastY;

	public CustomGLSurface(Context context)
	{
		super(context);

		renderer = new MyRenderer();
		setRenderer(renderer);
		setRenderMode(RENDERMODE_WHEN_DIRTY);
	}
	
	@Override 
	public boolean onTouchEvent(MotionEvent e) 
	{
		float y = e.getY();
		float x = e.getX();
		
		switch(e.getAction())
		{
		case MotionEvent.ACTION_MOVE:
			float diff = lastY - y;
			float height = this.getHeight();
			renderer.distance += (diff / height) * 5;
			
			diff = lastX - x;
			float width = this.getWidth();
			renderer.angle += (diff / width) * 180;
			
			requestRender();
			break;
		}

		lastX = x;
		lastY = y;
		
		return true;
	}

	/** The class that handles the OpenGL calls and actually draws things */
	private class MyRenderer implements GLSurfaceView.Renderer
	{
		public float distance, angle;
		
		private float vertices[] = { 
			-1, -1, 1, 
			1, -1, 1, 
			1, 1, 1, 
			-1, 1, 1,
			-1, -1, -1, 
			1, -1, -1, 
			1, 1, -1, 
			-1, 1, -1
		};

		private float colors[] = { 
			1, 0, 0, 1, // red
			0, 1, 0, 1, // green
			0, 0, 1, 1, // blue
			1, 0, 1, 1, // purple
			1, 1, 0, 1, // yellow
			0, 1, 1, 1, // cyan
			1, 1, 1, 1, // white
			0, 0, 0, 1, // black
		};

		private byte indices[] = { 
			// front
			0, 1, 3,	
			1, 2, 3,
			// back
			5, 4, 6,
			4, 6, 7,
			// left
			4, 0, 7,
			0, 3, 7,
			// right
			1, 5, 2,
			5, 6, 2
		};

		private FloatBuffer vertexbuffer, colorbuffer;
		private ByteBuffer indexbuffer;

		public MyRenderer()
		{
			distance = 3;
			angle = 0;
			
			// Buffers are needed to make sure that the garbage-collector doesn't throw away or move the arrays
			ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4); // 4 = size of float
			vbb.order(ByteOrder.nativeOrder());
			vertexbuffer = vbb.asFloatBuffer();
			vertexbuffer.put(vertices);
			vertexbuffer.position(0);

			ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * 4);
			cbb.order(ByteOrder.nativeOrder());
			colorbuffer = cbb.asFloatBuffer();
			colorbuffer.put(colors);
			colorbuffer.position(0);

			indexbuffer = ByteBuffer.allocateDirect(indices.length);
			indexbuffer.put(indices);
			indexbuffer.position(0);
		}

		public void onDrawFrame(GL10 gl)
		{
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

			gl.glMatrixMode(GL10.GL_MODELVIEW);
			gl.glLoadIdentity();
			
			gl.glTranslatef(0, 0, -distance);
			gl.glRotatef(-angle, 0, 1, 0);
			
			gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
			gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexbuffer);
			gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorbuffer);
			gl.glDrawElements(GL10.GL_TRIANGLES, indices.length, GL10.GL_UNSIGNED_BYTE, indexbuffer);
		}

		public void onSurfaceChanged(GL10 gl, int width, int height)
		{
			// Set the viewport to the entire screen (or at least the part allocated for the program)
			gl.glViewport(0, 0, width, height);

			// Set the frustum so that squares are actually square on the screen
			float ratio = (float) width / height;
			gl.glMatrixMode(GL10.GL_PROJECTION);
			gl.glLoadIdentity();
			gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
		}

		public void onSurfaceCreated(GL10 gl, EGLConfig config)
		{
			// By default, OpenGL enables features that improve quality but reduce performance. One might want to tweak that especially on software renderer.
			gl.glDisable(GL10.GL_DITHER);

			// Some one-time OpenGL initialization can be made here probably based on features of this particular context
			gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);
			gl.glClearColor(0, 0, .25f, 1);
			//gl.glEnable(GL10.GL_CULL_FACE);
			gl.glShadeModel(GL10.GL_SMOOTH);
			gl.glEnable(GL10.GL_DEPTH_TEST);
		}
	}

}