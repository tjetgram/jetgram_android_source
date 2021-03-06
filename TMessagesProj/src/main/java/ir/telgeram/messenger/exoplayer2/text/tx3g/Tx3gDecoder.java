/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ir.telgeram.messenger.exoplayer2.text.tx3g;

import ir.telgeram.messenger.exoplayer2.text.Cue;
import ir.telgeram.messenger.exoplayer2.text.SimpleSubtitleDecoder;
import ir.telgeram.messenger.exoplayer2.text.Subtitle;
import ir.telgeram.messenger.exoplayer2.util.ParsableByteArray;

/**
 * A {@link SimpleSubtitleDecoder} for tx3g.
 * <p>
 * Currently only supports parsing of a single text track.
 */
public final class Tx3gDecoder extends SimpleSubtitleDecoder {

  private final ParsableByteArray parsableByteArray;

  public Tx3gDecoder() {
    super("Tx3gDecoder");
    parsableByteArray = new ParsableByteArray();
  }

  @Override
  protected Subtitle decode(byte[] bytes, int length) {
    parsableByteArray.reset(bytes, length);
    int textLength = parsableByteArray.readUnsignedShort();
    if (textLength == 0) {
      return Tx3gSubtitle.EMPTY;
    }
    String cueText = parsableByteArray.readString(textLength);
    return new Tx3gSubtitle(new Cue(cueText));
  }

}
