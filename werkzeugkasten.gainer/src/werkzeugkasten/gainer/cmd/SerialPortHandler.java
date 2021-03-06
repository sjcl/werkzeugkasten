package werkzeugkasten.gainer.cmd;

import gnu.io.CommPort;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import werkzeugkasten.gainer.conf.Configuration;
import werkzeugkasten.gainer.interpreter.Interpreter;
import werkzeugkasten.gainer.lifecycle.LifeCycle;
import werkzeugkasten.gainer.util.CommPortUtil;

public class SerialPortHandler implements SerialPortEventListener, LifeCycle {

	protected static final Logger LOG = LoggerFactory
			.getLogger(SerialPortHandler.class);

	protected static final int rate = 38400;
	protected static final int databits = 8;
	protected static final int stopbits = SerialPort.STOPBITS_1;
	protected static final int parity = SerialPort.PARITY_NONE;

	protected Configuration config;
	protected SerialPort port;
	protected OutputStream out;
	protected StringBuffer listenBuffer = new StringBuffer(256);
	protected Interpreter interpreter;
	protected AtomicInteger numOfCmd = new AtomicInteger(0);

	public SerialPortHandler(Configuration config, Interpreter interpreter) {
		this.config = config;
		this.interpreter = interpreter;
	}

	@Override
	public void initialize() {
		try {
			CommPort cp = CommPortUtil.open(config.portId());
			this.port = (SerialPort) cp;
			this.port.setSerialPortParams(rate, databits, stopbits, parity);
			byte[] cmd = config.type().command();
			out = port.getOutputStream();
			CommPortUtil.emit(out, cmd);
			byte[] result = CommPortUtil.read(port);
			if (Arrays.equals(cmd, result) == false) {
				LOG.warn("Configuration and result is difficult command["
						+ new String(cmd) + "] result[ " + new String(result)
						+ "]");
			}
			this.port.addEventListener(this);
			this.port.notifyOnDataAvailable(true);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void dispose() {
		long now = System.currentTimeMillis();
		while (0 < this.numOfCmd.get()) {
			// 処理待ち
			if (10000 < (System.currentTimeMillis() - now)) {
				break;
			}
		}
		try {
			if (this.port != null) {
				this.port.removeEventListener();
				this.out.close();
				this.port.getInputStream().close();
				this.port.close();
			}
		} catch (IOException e) {
			LOG.debug(e.getMessage(), e);
		}
	}

	@Override
	public void serialEvent(SerialPortEvent event) {
		if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				InputStream in = this.port.getInputStream();
				int size = in.available();
				byte[] data = new byte[size];
				in.read(data);

				this.listenBuffer.append(new String(data));
				for (int i = this.listenBuffer.indexOf("*"); -1 < i;) {
					String cmd = listenBuffer.substring(0, i + 1);
					if (LOG.isDebugEnabled()) {
						LOG.debug("CMD [" + cmd + "]");
					}
					this.numOfCmd.decrementAndGet();
					this.interpreter.process(cmd);
					this.listenBuffer.delete(0, i + 1);
					i = this.listenBuffer.indexOf("*");
				}
			} catch (Exception e) {
				LOG.warn(e.getMessage(), e);
			}
		}
	}

	public void execute(Command cmd) {
		this.numOfCmd.incrementAndGet();
		cmd.emit(this.config, this.out);
	}

}
