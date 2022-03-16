package com.diamon.nucleo;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.diamon.dato.Configuraciones;
import com.diamon.recurso.Recurso;

public abstract class Juego extends Canvas
		implements Runnable, KeyListener, MouseMotionListener, MouseListener, WindowStateListener {

	private static final long serialVersionUID = 1L;

	private float anchoPantalla;

	private float altoPantalla;

	private float fps;

	private Thread hilo;

	private String tituloJuego;

	private volatile boolean iniciar;

	private final static int UNIDAD_TIEMPO = 1000000000;

	private double delta = 0;

	private final static byte CICLOS = 60;

	private final static double LIMITE_CICLOS = UNIDAD_TIEMPO / CICLOS;

	private BufferStrategy bufer;

	private JFrame ventana;

	private JPanel panel;

	private Pantalla pantalla;

	private Recurso recurso;

	private Configuraciones configuracion;

	private GraphicsDevice dispositivo;

	private BufferedImage cursor;

	private Cursor c;

	private boolean reajustar;

	public Juego(GraphicsConfiguration configuracion) {
		super(configuracion);

	}

	public Juego() {

		anchoPantalla = 640;

		altoPantalla = 480;

		tituloJuego = "Juego";

		fps = 60;

		reajustar = false;

		setBounds(0, 0, (int) anchoPantalla, (int) altoPantalla);

		setBackground(Color.BLACK);

		recurso = new Recurso();

		configuracion = new Configuraciones(Configuraciones.LOCAL);

		configuracion = configuracion.cargarConfiguraciones();

		if (configuracion.isLeerDatosInternos()) {

			Configuraciones configuracionInterna = new Configuraciones(Configuraciones.INTERNO);

			configuracion = configuracionInterna.cargarConfiguraciones();

			configuracion.setLeerDatosInternos(false);

			configuracion.guardarConfiguraciones();

		}

		requestFocus();

		setFocusable(true);

		setIgnoreRepaint(true);

		addKeyListener(this);

		addMouseMotionListener(this);

		addMouseListener(this);

		hilo = new Thread(this);

		ventana = new JFrame(tituloJuego);

		ventana.setBounds(0, 0, (int) anchoPantalla, (int) altoPantalla);

		ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		ventana.addWindowStateListener(this);

		panel = (JPanel) ventana.getContentPane();

		panel.setPreferredSize(new Dimension((int) anchoPantalla / 2, (int) altoPantalla / 2));

		panel.setLayout(null);

		panel.add(this);

		 ventana.setUndecorated(true);

		// ventana.setResizable(false);

		ventana.setIconImage(recurso.cargarImagen("logo.png"));

		ventana.setVisible(true);

		ventana.setLocationRelativeTo(null);

		GraphicsEnvironment graficosLocales = GraphicsEnvironment.getLocalGraphicsEnvironment();

		dispositivo = graficosLocales.getDefaultScreenDevice();

		DisplayMode[] dis = dispositivo.getDisplayModes();

		// dispositivo.setFullScreenWindow(ventana);

		for (int i = 0; i < dis.length; i++) {

			if (dis != null && dispositivo.isDisplayChangeSupported()) {

				try {

					if ((dis[i].getWidth() == (int) anchoPantalla) && (dis[i].getHeight() == (int) altoPantalla)
							&& (dis[i].getBitDepth() == 32) && (dis[i].getRefreshRate() == 60)) {

						dispositivo.setDisplayMode(dis[i]);

					}

				} catch (Exception e) {

				}

			}

		}

		createBufferStrategy(2);

		bufer = getBufferStrategy();

		iniciar = false;

		pantalla = null;

		iniciar();

	}

	public String getTituloJuego() {
		return tituloJuego;
	}

	@Override
	public void windowStateChanged(WindowEvent ev) {

		anchoPantalla = ev.getWindow().getSize().width;

		altoPantalla = ev.getWindow().getSize().height;

		setBounds(0, 0, (int) anchoPantalla, (int) altoPantalla);

		reajustar = true;

	}

	public void setTituloJuego(String tituloJuego) {

		if (ventana != null) {

			ventana.setTitle(tituloJuego);

		}

		this.tituloJuego = tituloJuego;
	}

	public float getFps() {
		return fps;
	}

	public void setFps(float fps) {
		this.fps = fps;
	}

	public float getAnchoPantalla() {
		return anchoPantalla;
	}

	public void setAnchoPantalla(float anchoPantalla) {
		this.anchoPantalla = anchoPantalla;
	}

	public float getAltoPantalla() {
		return altoPantalla;
	}

	public void setAltoPantalla(float altoPantalla) {
		this.altoPantalla = altoPantalla;
	}

	@Override
	public void run() {

		double referencia = System.nanoTime();

		final Graphics2D pincel = (Graphics2D) bufer.getDrawGraphics();

		Graphics2D pincel2 = pincel;

		while (iniciar) {

			if (reajustar) {

				pincel2 = (Graphics2D) bufer.getDrawGraphics();

				reajustar = false;
			}

			final double tiempoInicial = System.nanoTime();

			delta = (float) (tiempoInicial - referencia) / UNIDAD_TIEMPO;

			// Aquí actualización y dibújo

			pincel2.setColor(Color.BLUE);

			pincel2.fillRect(0, 0, (int) anchoPantalla, (int) altoPantalla);

			colisiones();

			actualizar((float) delta);

			renderizar(pincel2, (float) delta);

			bufer.show(); // Hasta aquí

			referencia = tiempoInicial;

			// Limite de cuadros

			do {

				Thread.yield();

			} while (System.nanoTime() - tiempoInicial < LIMITE_CICLOS);

		}

	}

	public void iniciar() {

		hilo.start();

		iniciar = true;

	}

	public void renderizar(Graphics2D pincel, float delta) {
		if (pantalla != null) {
			pantalla.dibujar(pincel, delta);

		}

	}

	public void actualizar(float delta) {
		if (pantalla != null) {
			pantalla.actualizar(delta);

		}
	}

	public void colisiones() {
		if (pantalla != null) {
			pantalla.colisiones();

		}

	}

	public void reajustarPantalla(int ancho, int alto) {
		if (pantalla != null) {
			pantalla.reajustarPantalla(ancho, ancho);
		}
	}

	public void resumen() {
		if (pantalla != null) {

			iniciar = true;
			hilo = new Thread(this);
			hilo.start();
			pantalla.resume();
		}
	}

	public void pausa() {
		if (pantalla != null) {

			pantalla.pausa();
			iniciar = false;
			while (true) {
				try {
					hilo.join();

					return;

				} catch (InterruptedException e) {

				}

			}

		}
	}

	public void liberarRecursos() {
		if (pantalla != null) {
			pantalla.ocultar();
		}
	}

	public void setPantalla(Pantalla pantalla) {
		if (this.pantalla != null) {
			this.pantalla.ocultar();
		}

		this.pantalla = pantalla;

		if (this.pantalla != null) {
			this.pantalla.mostrar();
			this.pantalla.reajustarPantalla(getWidth(), getHeight());

		}
	}

	public Pantalla getPantalla() {
		return pantalla;

	}

	@Override
	public void keyPressed(KeyEvent ev) {
		if (pantalla != null) {
			pantalla.teclaPresionada(ev);

		}

		switch (ev.getKeyCode()) {

		case KeyEvent.VK_Q:

			break;

		case KeyEvent.VK_W:

			break;

		case KeyEvent.VK_ESCAPE:

			System.exit(0);

			break;

		default:

			break;

		}

	}

	@Override
	public void keyReleased(KeyEvent ev) {
		if (pantalla != null) {
			pantalla.teclaLevantada(ev);

		}

	}

	@Override
	public void mouseDragged(MouseEvent ev) {

		if (pantalla != null) {
			pantalla.ratonDeslizando(ev);

		}

	}

	@Override
	public void mouseMoved(MouseEvent ev) {
		if (pantalla != null) {
			pantalla.ratonMoviendo(ev);

		}

	}

	@Override
	public void mouseClicked(MouseEvent ev) {
		if (pantalla != null) {
			pantalla.ratonClick(ev);

		}

	}

	@Override
	public void mouseEntered(MouseEvent ev) {

	}

	@Override
	public void mouseExited(MouseEvent ev) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent ev) {

		if (pantalla != null) {

			pantalla.ratonPresionado(ev);

		}

	}

	public void setCursor(String nombreImagen, float ancho, float alto) {

		cursor.getGraphics().drawImage(recurso.getImagen(nombreImagen), 0, 0, (int) ancho, (int) alto, this);

		Toolkit t = Toolkit.getDefaultToolkit();

		c = t.createCustomCursor(cursor, new Point(5, 5), "null");

		setCursor(c);

	}

	public void ocultarCursor(boolean ocultar) {

		if (ocultar) {

			cursor = recurso.createCompatible(10, 10, Transparency.BITMASK);

			Toolkit t = Toolkit.getDefaultToolkit();

			c = t.createCustomCursor(this.cursor, new Point(5, 5), "null");

			setCursor(c);

		} else {

			if (c != null) {

				setCursor(c);

			}

		}

	}

	@Override
	public void mouseReleased(MouseEvent ev) {

		if (pantalla != null) {

			pantalla.ratonLevantado(ev);

		}

	}

	@Override
	public void keyTyped(KeyEvent ev) {
		if (pantalla != null) {

			pantalla.teclaTipo(ev);

		}

	}

	public void setPantallaCompleta(boolean pantallaCompleta) {

		if (pantallaCompleta) {

			DisplayMode[] dis = dispositivo.getDisplayModes();

			dispositivo.setFullScreenWindow(ventana);

			for (int i = 0; i < dis.length; i++) {

				if (dis != null && dispositivo.isDisplayChangeSupported()) {

					try {

						if ((dis[i].getWidth() == 640) && (dis[i].getHeight() == 480) && (dis[i].getBitDepth() == 32)
								&& (dis[i].getRefreshRate() == 60)) {

							dispositivo.setDisplayMode(dis[i]);

						}

					} catch (Exception e) {

					}

				}

			}

		}

		else {

			dispositivo.setFullScreenWindow(null);

		}

	}

	public Recurso getRecurso() {
		return recurso;
	}

	public Configuraciones getConfiguracion() {
		return configuracion;
	}

	public void parar() {
		iniciar = false;
	}

}
