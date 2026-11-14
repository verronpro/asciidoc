import org.docx4j.wml.*;
import java.math.BigInteger;
var f = org.docx4j.jaxb.Context.getWmlObjectFactory();
var b = f.createCTBorder();
b.setVal(STBorder.fromValue("single"));
b.setSz(BigInteger.valueOf(24));
b.setSpace(BigInteger.valueOf(24));
b.setColor("auto");
System.out.println("fresh: shadow=" + b.isShadow() + " frame=" + b.isFrame());
var sect = f.createSectPr();
var pb = f.createSectPrPgBorders();
pb.setOffsetFrom(STPageBorderOffset.PAGE);
pb.setTop(b);
sect.setPgBorders(pb);
System.out.println("after wiring: shadow=" + sect.getPgBorders().getTop().isShadow() + " frame=" + sect.getPgBorders().getTop().isFrame());
/exit
