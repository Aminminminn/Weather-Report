/*
 * Java
 *
 * Copyright 2025 HAJJI Amin
 * Use of this source code is governed by a BSD-style license that can be found with this software.
 */

package com.microej.weatherreport.ui;

import ej.microui.display.Colors;
import ej.microui.display.Font;
import ej.microui.display.GraphicsContext;
import ej.microui.display.Image;
import ej.microui.display.Painter;
import ej.mwt.Widget;
import ej.mwt.util.Size;
import ej.widget.render.ImagePainter;
import ej.widget.render.StringPainter;

/**
 * Widget class used to show a loading screen during the board/app startup.
 */
public class LoadingScreenWidget extends Widget {
	@Override
	protected void computeContentOptimalSize(Size size) {
		// do nothing
	}

	@Override
	protected void renderContent(GraphicsContext g, int contentWidth, int contentHeight) {
		g.setColor(Colors.WHITE);
		Painter.fillRectangle(g, 0, 0, contentWidth, contentHeight);

		int imageSize = 64;
		ImagePainter.drawImageInArea(g, Image.getImage("/images/clear-day.png"), (contentWidth / 2) - imageSize,
				(contentHeight / 2) - imageSize, imageSize, imageSize, 0, 0);

		String loadingText = "Loading...";
		Font font = Font.getDefaultFont();

		g.setColor(Colors.BLACK);
		StringPainter.drawStringAtPoint(g, loadingText, font, (contentWidth / 2) - (font.stringWidth(loadingText) / 2),
				(contentHeight / 2) + imageSize, 0, 0);
	}
}
