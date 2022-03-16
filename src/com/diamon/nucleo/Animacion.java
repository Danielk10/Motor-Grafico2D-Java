package com.diamon.nucleo;

import java.awt.image.BufferedImage;

public class Animacion {

	public static final int REPETIR = 0;

	public static final int NORMAL = 1;

	private final BufferedImage[] imagenes;

	private final float duracionCuadros;

	private int modo;

	public Animacion(float duracionCuadros, BufferedImage... imagenes) {

		this.duracionCuadros = duracionCuadros;

		this.imagenes = imagenes;

		modo = NORMAL;
	}

	public int getModo() {
		return modo;
	}

	public void setModo(int modo) {
		this.modo = modo;
	}

	public BufferedImage getKeyFrame(float tiempo) {

		int numeroCuadros = (int) (tiempo / duracionCuadros);

		if (modo == NORMAL) {

			numeroCuadros = Math.min(imagenes.length - 1, numeroCuadros);
		}

		if (modo == REPETIR) {

			numeroCuadros = numeroCuadros % imagenes.length;
		}
		return imagenes[numeroCuadros];
	}
}
