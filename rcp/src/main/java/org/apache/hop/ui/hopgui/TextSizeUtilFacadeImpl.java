/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hop.ui.hopgui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

public class TextSizeUtilFacadeImpl extends TextSizeUtilFacade {

  @Override
  Point textExtentInternal( Font font, String text, int wrapWidth ) {
    Image dummyImage = new Image( Display.getCurrent(), 50, 10 );
    GC dummyGC = new GC( dummyImage );
    Point point = dummyGC.textExtent( text, SWT.DRAW_TAB | SWT.DRAW_DELIMITER );
    dummyImage.dispose();
    dummyGC.dispose();
    return point;
  }

}
