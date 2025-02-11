/* Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 */
package org.meteoinfo.geo.layout;

import org.meteoinfo.common.*;
import org.meteoinfo.geo.drawing.Draw;
import org.meteoinfo.geometry.graphic.ResizeAbility;
import org.meteoinfo.geometry.legend.*;
import org.meteoinfo.ui.event.IMapViewUpdatedListener;
import org.meteoinfo.ui.event.ISizeChangedListener;
import org.meteoinfo.ui.event.MapViewUpdatedEvent;
import org.meteoinfo.ui.event.SizeChangedEvent;
import org.meteoinfo.geo.layer.LayerDrawType;
import org.meteoinfo.geo.layer.LayerTypes;
import org.meteoinfo.geo.layer.MapLayer;
import org.meteoinfo.geo.layer.VectorLayer;
import org.meteoinfo.geometry.graphic.Graphic;
import org.meteoinfo.geometry.shape.PointShape;
import org.meteoinfo.geometry.shape.ShapeTypes;
import org.meteoinfo.geometry.shape.WindArrow;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.meteoinfo.geometry.shape.EllipseShape;

/**
 *
 * @author yaqiang
 */
public class LayoutGraphic extends LayoutElement {
// <editor-fold desc="Variables">

    private MapLayout _mapLayout;
    private LayoutMap _layoutMap;
    private Graphic _graphic;
    private boolean _updatingSize = false;
    private boolean _isTitle = false;
    private boolean _isPaint;
    private boolean _antiAlias = true;
    // </editor-fold>
    // <editor-fold desc="Constructor">

    /**
     * Constructor
     *
     * @param aGraphic Graphic
     * @param aMapLayout MapLayout
     */
    public LayoutGraphic(Graphic aGraphic, MapLayout aMapLayout) {
        super();
        this.setElementType(ElementType.LAYOUT_GRAPHIC);
        this.setResizeAbility(ResizeAbility.RESIZE_ALL);

        _mapLayout = aMapLayout;
        _isPaint = true;
        this.setGraphic(aGraphic);
        if (_graphic.getLegend() != null) {
            if (_graphic.getLegend().getBreakType() == BreakTypes.LABEL_BREAK) {
                ((LabelBreak) _graphic.getLegend()).addSizeChangedListener(new ISizeChangedListener() {
                    @Override
                    public void sizeChangedEvent(SizeChangedEvent event) {
                        updateControlSize();
                    }
                });
            }
        }
    }

    /**
     * Constructor
     *
     * @param aGraphic Graphic
     * @param aMapLayout MapLayout
     * @param aLayoutMap LayoutMap
     */
    public LayoutGraphic(Graphic aGraphic, MapLayout aMapLayout, LayoutMap aLayoutMap) {
        super();
        this.setElementType(ElementType.LAYOUT_GRAPHIC);
        this.setResizeAbility(ResizeAbility.RESIZE_ALL);

        _mapLayout = aMapLayout;
        _isPaint = true;
        this.setGraphic(aGraphic);
        if (_graphic.getLegend() != null) {
            if (_graphic.getLegend().getBreakType() == BreakTypes.LABEL_BREAK) {
                ((LabelBreak) _graphic.getLegend()).addSizeChangedListener(new ISizeChangedListener() {
                    @Override
                    public void sizeChangedEvent(SizeChangedEvent event) {
                        updateControlSize();
                    }
                });
            }
        }

        _layoutMap = aLayoutMap;
        _layoutMap.addMapViewUpdatedListener(new IMapViewUpdatedListener() {
            @Override
            public void mapViewUpdatedEvent(MapViewUpdatedEvent event) {
                if (_graphic.getLegend().getBreakType() == BreakTypes.VECTOR_BREAK) {
                    for (int i = 0; i < _layoutMap.getMapFrame().getMapView().getLayerNum(); i++) {
                        MapLayer aLayer = _layoutMap.getMapFrame().getMapView().getLayers().
                                get(_layoutMap.getMapFrame().getMapView().getLayerNum() - 1 - i);
                        if (aLayer.getLayerType() == LayerTypes.VECTOR_LAYER) {
                            if (aLayer.isVisible() && aLayer.getLayerDrawType() == LayerDrawType.VECTOR) {
                                setVisible(true);
                                float zoom = ((VectorLayer) aLayer).getDrawingZoom();
                                ((VectorBreak) _graphic.getLegend()).setZoom(zoom);
//                                float max = 30.0f / zoom;
//                                WindArraw aWA = (WindArraw) _graphic.getShape();
//                                int llen = 5;
//                                for (i = 10; i <= 100; i += 5) {
//                                    if (max < i) {
//                                        llen = i - 5;
//                                        break;
//                                    }
//                                }
//                                aWA.length = llen;
//                                aWA.size = 5;
//                                aWA.setValue(0);
                                updateControlSize();
                                break;
                            }
                        }
                    }
                }
            }
        });
    }
    // </editor-fold>
    // <editor-fold desc="Get Set Methods">

    public Graphic getGraphic() {
        return _graphic;
    }

    public void setGraphic(Graphic graphic) {
        _graphic = graphic;
        if (_graphic.getShape() != null) {
            switch (_graphic.getShape().getShapeType()) {
                case POINT:
                    if (_graphic.getLegend().getBreakType() == BreakTypes.POINT_BREAK) {
                        this.setResizeAbility(ResizeAbility.SAME_WIDTH_HEIGHT);
                    } else if (_graphic.getLegend().getBreakType() == BreakTypes.LABEL_BREAK) {
                        this.setResizeAbility(ResizeAbility.NONE);
                    }
                    break;
                case CIRCLE:
                    this.setResizeAbility(ResizeAbility.SAME_WIDTH_HEIGHT);
                    break;
                case WIND_ARROW:
                    this.setResizeAbility(ResizeAbility.NONE);
                    break;
                default:
                    this.setResizeAbility(ResizeAbility.RESIZE_ALL);
                    break;
            }
            updateControlSize();
        }
    }

    /**
     * Get if is title
     *
     * @return If is title
     */
    public boolean isTitle() {
        return _isTitle;
    }

    /**
     * Set if is title
     *
     * @param istrue Boolean
     */
    public void setIsTitle(boolean istrue) {
        _isTitle = istrue;
    }

    /**
     * Get if paint
     *
     * @return If paint
     */
    public boolean isPaint() {
        return _isPaint;
    }

    /**
     * Set if paint
     *
     * @param istrue If paint
     */
    public void setIsPaint(boolean istrue) {
        _isPaint = istrue;
    }

    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Set label text
     *
     * @param text Label text
     */
    public void setLabelText(String text) {
        switch (_graphic.getShape().getShapeType()) {
            case POINT:
                if (_graphic.getLegend().getBreakType() == BreakTypes.LABEL_BREAK) {
                    ((LabelBreak) _graphic.getLegend()).setText(text);
                    updateControlSize();
                }
                break;
        }
    }

    /**
     * Update control size
     */
    public void updateControlSize() {
        if (_graphic.getShape() == null) {
            return;
        }

        _updatingSize = true;

        switch (_graphic.getShape().getShapeType()) {
            case POINT:
                PointShape aPS = (PointShape) _graphic.getShape();
                this.setLeft((int) aPS.getPoint().X);
                this.setTop((int) aPS.getPoint().Y);
                if (_graphic.getLegend().getBreakType() == BreakTypes.POINT_BREAK) {
                    PointBreak aPB = (PointBreak) _graphic.getLegend();
                    this.setLeft(this.getLeft() - (int) (aPB.getSize() / 2));
                    this.setTop(this.getTop() - (int) (aPB.getSize() / 2));
                    this.setWidth((int) Math.ceil(aPB.getSize()));
                    this.setHeight((int) Math.ceil(aPB.getSize()));
                } else if (_graphic.getLegend().getBreakType() == BreakTypes.LABEL_BREAK) {
                    LabelBreak aLB = (LabelBreak) _graphic.getLegend();
                    //FontMetrics metrics = _mapLayout.getGraphics().getFontMetrics(aLB.getFont());
                    BufferedImage image = new BufferedImage(_mapLayout.getPageBounds().width, _mapLayout.getPageBounds().height, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = (Graphics2D)image.getGraphics();
                    g.setFont(aLB.getFont());
                    Dimension aSF = Draw.getStringDimension(aLB.getTexts(), aLB.getLineSpace(), g);
                    //FontMetrics metrics = image.getGraphics().getFontMetrics(aLB.getFont());
                    //Dimension aSF = new Dimension(metrics.stringWidth(aLB.getText()), metrics.getHeight());
                    this.setLeft(this.getLeft() - (int) (aSF.width / 2));
                    this.setTop(this.getTop() - (int) (aSF.height * 2 / 3));
                    this.setWidth((int) Math.ceil(aSF.width));
                    this.setHeight((int) Math.ceil(aSF.getHeight()));
                }
                break;
            case WIND_ARROW:
                    WindArrow aWA = (WindArrow)_graphic.getShape();
                    this.setLeft((int)aWA.getPoint().X);
                    this.setTop((int)aWA.getPoint().Y);
                    if (aWA.length == 0){
                        aWA.length = 20;
                    }
                    this.setWidth((int)(aWA.length * ((VectorBreak)_graphic.getLegend()).getZoom()));
                    this.setHeight(20);
                break;
            case POLYLINE:
            case POLYGON:
            case RECTANGLE:
            case CIRCLE:
            case CURVE_LINE:
            case CURVE_POLYGON:
            case ELLIPSE:
                Extent extent = _graphic.getShape().getExtent();
                this.setLeft((int) Math.ceil(extent.minX));
                this.setTop((int) Math.ceil(extent.minY));
                this.setWidth((int) Math.ceil(extent.getWidth()));
                this.setHeight((int) Math.ceil((extent.getHeight())));
                break;
        }

        _updatingSize = false;
    }

    /**
     * Vertice edited update
     *
     * @param vIdx Vertice index
     * @param newX New x
     * @param newY New y
     */
    public void verticeEditUpdate(int vIdx, double newX, double newY) {
        List<PointD> points = (List<PointD>) _graphic.getShape().getPoints();
        switch (_graphic.getShape().getShapeType()) {
            case POLYGON:
            case CURVE_POLYGON:
            case RECTANGLE:
                int last = points.size() - 1;
                if (vIdx == 0) {
                    if (points.get(0).X == points.get(last).X && points.get(0).Y == points.get(last).Y) {
                        points.get(last).X = newX;
                        points.get(last).Y = newY;
                    }
                } else if (vIdx == last) {
                    if (points.get(0).X == points.get(last).X && points.get(0).Y == points.get(last).Y) {
                        points.get(0).X = newX;
                        points.get(0).Y = newY;
                    }
                }
                break;
        }

        PointD aP = points.get(vIdx);
        aP.X = newX;
        aP.Y = newY;
        _graphic.getShape().setPoints(points);
        updateControlSize();
    }

    @Override
    public void paint(Graphics2D g) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void paintOnLayout(Graphics2D g, PointF pageLocation, float zoom) {
        if (this.isDrawBackColor()){
            PointF aP = pageToScreen(this.getLeft(), this.getTop(), pageLocation, zoom);        
            Rectangle rect = new Rectangle((int) aP.X, (int) aP.Y, (int) (this.getWidth() * zoom), (int) (this.getHeight() * zoom));
            g.setColor(this.getBackColor());
            g.fill(rect);
        }

        //Draw graphics
        if (_antiAlias) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        } else {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }
        paintGraphics(g, pageLocation, zoom);
    }

    /**
     * Paint graphics
     *
     * @param g Graphics2D
     * @param pageLocation page location
     * @param zoom Zoom
     */
    public void paintGraphics(Graphics2D g, PointF pageLocation, float zoom) {
        switch (_graphic.getShape().getShapeType()) {
            case POINT:
                PointD dPoint = _graphic.getShape().getPoints().get(0);
                PointF aPoint = pageToScreen((float) dPoint.X, (float) dPoint.Y, pageLocation, zoom);
                if (_graphic.getLegend().getBreakType() == BreakTypes.POINT_BREAK) {
                    PointBreak aPB = (PointBreak) ((PointBreak) _graphic.getLegend()).clone();
                    float size = aPB.getSize();
                    aPB.setSize(aPB.getSize() * zoom);
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Draw.drawPoint(aPoint, aPB, g);
                    aPB.setSize(size);
                    if (!_antiAlias) {
                        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    }
                } else if (_graphic.getLegend().getBreakType() == BreakTypes.LABEL_BREAK) {
                    LabelBreak aLB = (LabelBreak) ((LabelBreak) _graphic.getLegend()).clone();
                    Font font = new Font(aLB.getFont().getFontName(), aLB.getFont().getStyle(), aLB.getFont().getSize());
                    aLB.setFont(new Font(font.getFontName(), font.getStyle(), (int) (font.getSize() * zoom)));
                    Rectangle rect = new Rectangle();
                    Draw.drawLabelPoint(aPoint, aLB, g, rect);
                    this.setBounds(rect);
                    aLB.setFont(font);
                }
                break;
            case WIND_ARROW:
                dPoint = _graphic.getShape().getPoints().get(0);
                aPoint = pageToScreen((float) dPoint.X, (float) dPoint.Y, pageLocation, zoom);
                WindArrow aArraw = (WindArrow) _graphic.getShape();
                VectorBreak aVB = (VectorBreak) _graphic.getLegend();
                Draw.drawArraw(aVB.getColor(), aPoint, aArraw, g, aVB.getZoom() * zoom);
                Font drawFont = new Font("Arial", Font.PLAIN, (int) (12 * zoom));
                FontMetrics metrics = g.getFontMetrics(drawFont);
                String drawStr = DataConvert.removeTailingZeros(String.valueOf(aArraw.length));
                Dimension fsize = new Dimension(metrics.stringWidth(drawStr), metrics.getHeight());
                g.setColor(aVB.getColor());
                g.setFont(drawFont);
                g.drawString(drawStr, aPoint.X, aPoint.Y + fsize.height);
                break;
            case POLYLINE:
            case POLYGON:
            case RECTANGLE:
            case CIRCLE:
            case CURVE_LINE:
            case CURVE_POLYGON:
            case ELLIPSE:
                List<PointD> pList = (List<PointD>) _graphic.getShape().getPoints();
                PointF[] points = new PointF[pList.size()];
                for (int i = 0; i < pList.size(); i++) {
                    points[i] = pageToScreen((float) pList.get(i).X, (float) pList.get(i).Y, pageLocation, zoom);
                }

                switch (_graphic.getShape().getShapeType()) {
                    case POLYLINE:
                        PolylineBreak aPLB = (PolylineBreak) ((PolylineBreak) _graphic.getLegend()).clone();
                        float size = aPLB.getWidth();
                        aPLB.setWidth(size * zoom);
                        Draw.drawPolyline(points, (PolylineBreak) _graphic.getLegend(), g);
                        aPLB.setWidth(size);
                        break;
                    case POLYGON:
                    case RECTANGLE:
                        PolygonBreak aPGB = (PolygonBreak) ((PolygonBreak) _graphic.getLegend()).clone();
                        size = aPGB.getOutlineSize();
                        aPGB.setOutlineSize(size * zoom);
                        Draw.drawPolygon(points, (PolygonBreak) _graphic.getLegend(), g);
                        aPGB.setOutlineSize(size);
                        break;
                    case CIRCLE:
                        aPGB = (PolygonBreak) ((PolygonBreak) _graphic.getLegend()).clone();
                        size = aPGB.getOutlineSize();
                        aPGB.setOutlineSize(size * zoom);
                        Draw.drawCircle(points, (PolygonBreak) _graphic.getLegend(), g);
                        aPGB.setOutlineSize(size);
                        break;
                    case CURVE_LINE:
                        aPLB = (PolylineBreak) ((PolylineBreak) _graphic.getLegend()).clone();
                        size = aPLB.getWidth();
                        aPLB.setWidth(size * zoom);
                        Draw.drawCurveLine(points, (PolylineBreak) _graphic.getLegend(), g);
                        aPLB.setWidth(size);
                        break;
                    case CURVE_POLYGON:
                        aPGB = (PolygonBreak) ((PolygonBreak) _graphic.getLegend()).clone();
                        size = aPGB.getOutlineSize();
                        aPGB.setOutlineSize(size * zoom);
                        Draw.drawCurvePolygon(points, (PolygonBreak) _graphic.getLegend(), g);
                        aPGB.setOutlineSize(size);
                        break;
                    case ELLIPSE:
                        aPGB = (PolygonBreak) ((PolygonBreak) _graphic.getLegend()).clone();
                        size = aPGB.getOutlineSize();
                        aPGB.setOutlineSize(size * zoom);
                        float angle = ((EllipseShape)_graphic.getShape()).getAngle();
                        Draw.drawEllipse(points, angle, (PolygonBreak) _graphic.getLegend(), g);
                        aPGB.setOutlineSize(size);
                        break;
                }
                break;
        }
    }

    @Override
    public void moveUpdate() {
        if (_graphic.getShape() != null) {
            List<PointD> points = (List<PointD>) _graphic.getShape().getPoints();
            Extent aExtent = _graphic.getShape().getExtent();
            double minX = aExtent.minX;
            double minY = aExtent.minY;
            if (_graphic.getShape().getShapeType() == ShapeTypes.POINT) {
                //minX -= this.getWidth() / 2;
                if (_graphic.getLegend().getBreakType() == BreakTypes.POINT_BREAK)
                    minY -= this.getHeight() / 2;
                else if (_graphic.getLegend().getBreakType() == BreakTypes.LABEL_BREAK)
                    minY -= this.getHeight() * 2 / 3;
            }
            int shiftX = this.getLeft() - (int) minX;
            int shiftY = this.getTop() - (int) minY;
            for (int i = 0; i < points.size(); i++) {
                PointD aP = points.get(i);
                aP.X += shiftX;
                aP.Y += shiftY;
            }
            _graphic.getShape().setPoints(points);
        }
    }

    @Override
    public void resizeUpdate() {
        if (_graphic.getShape() != null) {
            switch (_graphic.getShape().getShapeType()) {
                case POINT:
                    if (_graphic.getLegend().getBreakType() == BreakTypes.POINT_BREAK) {
                        PointBreak aPB = (PointBreak) _graphic.getLegend();
                        aPB.setSize(this.getWidth());
                        updateControlSize();
                    }
                    break;
                case POLYLINE:
                case POLYGON:
                case CURVE_LINE:
                case CURVE_POLYGON:
                    moveUpdate();
                    List<PointD> points = (List<PointD>) _graphic.getShape().getPoints();
                    Extent aExtent = _graphic.getShape().getExtent();
                    int deltaX = this.getWidth() - (int) aExtent.getWidth();
                    int deltaY = this.getHeight() - (int) aExtent.getHeight();

                    for (int i = 0; i < points.size(); i++) {
                        PointD aP = points.get(i);
                        aP.X = aP.X + deltaX * (aP.X - aExtent.minX) / aExtent.getWidth();
                        aP.Y = aP.Y + deltaY * (aP.Y - aExtent.minY) / aExtent.getHeight();
                    }
                    _graphic.getShape().setPoints(points);
                    break;
                case RECTANGLE:
                case ELLIPSE:
                    points = new ArrayList<>();
                    points.add(new PointD(this.getLeft(), this.getTop()));
                    points.add(new PointD(this.getLeft(), this.getBottom()));
                    points.add(new PointD(this.getRight(), this.getBottom()));
                    points.add(new PointD(this.getRight(), this.getTop()));
                    if (_graphic.getShape().getShapeType() == ShapeTypes.RECTANGLE) {
                        points.add((PointD) points.get(0).clone());
                    }
                    _graphic.getShape().setPoints(points);
                    break;
                case CIRCLE:
                    points = new ArrayList<>();
                    points.add(new PointD(this.getLeft(), this.getTop() + this.getWidth() / 2));
                    points.add(new PointD(this.getLeft() + this.getWidth() / 2, this.getTop()));
                    points.add(new PointD(this.getLeft() + this.getWidth(), this.getTop() + this.getWidth() / 2));
                    points.add(new PointD(this.getLeft() + this.getWidth() / 2, this.getTop() + this.getWidth()));
                    _graphic.getShape().setPoints(points);
                    break;
            }
        }
    }
    
    @Override
    public void fireLocationChangedEvent() {
        super.fireLocationChangedEvent();
        
        if (!this._updatingSize)
            this.moveUpdate();
    }
    // </editor-fold>        
}
