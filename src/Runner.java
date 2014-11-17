import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.sound.sampled.*;

public class Runner {

	public static void main(String[] args) throws LineUnavailableException, IOException, InterruptedException {
		String url = "/Users/Admin/Documents/School/UGS 303/3/data/trial2/";
		ArrayList<LinkedList<Double>> allData = new ArrayList<LinkedList<Double>>();
		File file = new File(url+"5-half"+".csv");
		FileWriter fw = new FileWriter(file);
		
		for(double bigi = 65.40639133; bigi<780; bigi = (bigi*Math.pow(2, 1/12.0))){
			
			for(int trial = 1; trial<=1; trial++){
				//set up the first bit of the arraylist
				LinkedList<Double> current = new LinkedList<Double>();
				current.add(bigi+0.0);
				//set up initlalizing stuff for the microphone
				AudioFormat format2 = new AudioFormat(8000.0f, 16, 1, true, true);
				TargetDataLine microphone = AudioSystem.getTargetDataLine(format2);
				DataLine.Info info = new DataLine.Info(TargetDataLine.class, format2);
				byte[] data = new byte[8*64*2*4];
				
				//set up the stuff to play a note
				boolean a = true;
				if(a){
					// following code from
					// http://stackoverflow.com/questions/11331485/is-there-an-library-in-java-for-emitting-a-certain-frequency-constantly
					AudioFormat format = new AudioFormat(44000f, 16, 1, true, false);
					SourceDataLine line = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, format));
					line.open(format);
					line.start();

					double f = bigi; // desired frequency of note
					System.out.print(f);
					double t = 5; // seconds the note plays for

					byte[] buffer = new byte[(int) (format.getSampleRate() * t * 2 + .5)];
					f *= Math.PI / format.getSampleRate();

					for(int i = 0; i<16; i++){
						buffer[i] = (byte) (i*2*Math.pow(-1, i));
					}
					
					for (int i = 16; i < buffer.length; i += 2) {
						int value = (int) (32767 * Math.sin(i * f));
						buffer[i + 1] = (byte) ((value >> 8) & 0xFF);
						buffer[i] = (byte) (value & 0xFF);
					}
					line.write(buffer, 0, buffer.length);
//					line.drain();
				}
				
				// end of stack overflow code
				// and this next code comes to us from the Orical Website. Kind of. it got changed a lot
				
				if (!AudioSystem.isLineSupported(info)) {
					System.out.println("Whoops 1");
				}
				try {
					microphone = (TargetDataLine) AudioSystem.getLine(info);
					microphone.open(format2);
				} catch (LineUnavailableException ex) {
					System.out.println("Whoops 2");
				}
				microphone.start();
				double t = System.currentTimeMillis();
				//=Thread.sleep(2000);
				
				
				for(int z = 0; z<25; z++){
					microphone.read(data, 0, data.length);
					double n = System.currentTimeMillis();
					System.out.print("\t"+average1(data));
					current.add(average1(data));
				}
				System.out.println("");
				allData.add(current);
			}
		}
		
		for(LinkedList<Double> l: allData){
			String thisLine = "";
			for(double d: l){
				thisLine = thisLine+ d+",";
			}
			thisLine = thisLine+"\n";
			fw.append(thisLine);
		}
		fw.flush();
		fw.close();
	}
	public static double average1( byte[] b){
		double sum = 0.0;
		for(byte by: b){
			sum+= Math.abs(by);
		}
		return sum/b.length;
	}
	
	public static double average (byte[] b){
		double sum = 0;
		int[] vals = new int[b.length/2];
		for(int i = 0; i<vals.length; i++){
			vals[i] = Math.abs(b[i])*256+Math.abs(b[i+1]);
		}
		for(int i: vals){
			sum += i;
		}
		return sum/(vals.length);
	}

}
