import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JOptionPane;

public class Tone {
public enum Channel {
    LEFT, RIGHT, STEREO
};

public static final float SAMPLE_RATE = 44104; // Should be a multiple of 8
protected byte[] buf;
protected int hz, msecs;
protected double vol;
protected Channel channel;

Tone() {
} // necessary so that subclasses don't complain

public Tone(int hz, int msecs, double vol, Tone.Channel channel) {
    if (hz <= 0)
        throw new IllegalArgumentException("Frequency <= 0 hz");
    if (msecs <= 0)
        throw new IllegalArgumentException("Duration <= 0 msecs");
    if (vol > 1.0 || vol < 0.0)
        throw new IllegalArgumentException("Volume out of range 0.0 - 1.0");
    this.channel = channel;
    this.hz = hz;
    this.vol = vol;
    this.msecs = msecs;
    generateTone();

}

private void generateTone() {
    int len = (int)Math.ceil((2 * SAMPLE_RATE * msecs / 1000.0d));
    if (len % 2 == 1)
        len = len + 1;
    buf = new byte[len];
    int fadeCount = 1600;
    for (int i = 0; i < buf.length /2; i++) {
        double fadeRate = 1.0;
        double angle = (i * hz / SAMPLE_RATE) * 2.0 * Math.PI;
        if (i<fadeCount) {
            fadeRate = (double)i/(double)fadeCount;
        } else if (i>(buf.length/2)-fadeCount) {
            int bufLength = buf.length;
            int buf = bufLength/2;
            int countDown = buf-i;
            fadeRate = (double)countDown/(double)(fadeCount);
        }
        buf[2*i + 1] = buf[2*i] = (byte) Math.round(
            Math.cos(angle) * 127.0 * vol * fadeRate);
    }
}

public void play(SourceDataLine sdl) { // takes an opened SourceDataLine
    FloatControl panControl = (FloatControl) sdl
            .getControl(FloatControl.Type.PAN);
    if (panControl != null) { // Preferred method using built in sound
                                // control, but not guaranteed to be
                                // available
        if (channel == Channel.LEFT) {
            panControl.setValue(-1);
        } else if (channel == Channel.RIGHT) {
            panControl.setValue(1);
        } else {
            panControl.setValue(0);
        }
    } else { // fallback method is directly manipulates the buffer
        if (channel != Channel.STEREO) {
            int nSilenceOffset;
            byte nSilenceValue = 0;
            if (channel == Channel.LEFT) {
                nSilenceOffset = 1;
            } else {
                nSilenceOffset = 0;
            }
            for (int i = 0; i < buf.length; i += 2) {
                buf[i + nSilenceOffset] = nSilenceValue;
            }
        }

    }
    sdl.write(buf, 0, buf.length);
    sdl.drain();
}

public static void main(String[] args) {
    AudioFormat af = new AudioFormat(Tone.SAMPLE_RATE, 8, 2, true, false);
    SourceDataLine sdl;
    try {
        sdl = AudioSystem.getSourceDataLine(af);
    } catch (LineUnavailableException e) {
        JOptionPane.showMessageDialog(null, "Couldn't get sound line");
        return;
    }
    try {
        sdl.open(af);
    } catch (LineUnavailableException e) {
        JOptionPane.showMessageDialog(null, "Couldn't open sound line");
        return;
    }
    sdl.start();
    Tone left = new Tone(400, 2000, .5, Tone.Channel.STEREO);
    System.out.println("Playing left");
    long t = System.currentTimeMillis();
    left.play(sdl);
    sdl.stop();
    sdl.close();
}

}