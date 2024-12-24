package io.github.taodong.mail.dkim;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.FieldSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

class DkimSigningServiceTest {

    private static final Date TEST_DATE = createTestDate();

    private final DkimSigningService dkimSigningService = new DkimSigningService();
    private final DkimMimeMessageHelper dkimMimeMessageHelper = new DkimMimeMessageHelper();
    private final RSAPrivateKey testKey = getTestKey();
    private final PublicKey testPublicKey = getTestPublicKey();

    @SuppressWarnings("unused")
    private static final List<Arguments> signTestCases = List.of(
        argumentSet("Empty message",
                createTestMessage("tao.dong@duotail.com", "test@gmail.com", "Empty Body", ""),
                "s1", "duotail.com", "tao.dong@duotail.com", null, null,
                "v=1; a=rsa-sha256; d=duotail.com; c=simple/simple; i=tao.dong@duotail.com; s=s1; h=From:To:Subject:Date; bh=frcCV1k9oG9oKj3dpUqdJg1PxRT2RSN/XKdLCPjaYaY=; b="),
        argumentSet("Random string message",
                createTestMessage("tao.dong1@duotail.com", "test1@gmail.com", "Random String",
                        """
                                2mLTGnlOh99hofrl74daqtyAK6BbZ8HXcFupX19kIveOaOIwY0XKsRUDAxZ4g810iTXnIB1ouXwabgDSMER0Y
                                  o9qUM00tl64g9khiJgxAkEaGfRlo5JAPt2j1xSfzvAOXIT0BmkTwCM2I6zYm7N9ZzmF9tGtrxXSq28mLK8vDY6fLixb3Iw8s
                                  JLsW4qS2AiG7LSnC2yAcqGLTlVd9J
                              """),
                "s2", "duotail.com", "tao.dong1@duotail.com", Canonicalization.RELAXED, Canonicalization.RELAXED,
                "v=1; a=rsa-sha256; d=duotail.com; c=relaxed/relaxed; i=tao.dong1@duotail.com; s=s2; h=From:To:Subject:Date; bh=09fALaVrZwJFqxnBCzhWsPpCxjs6VthdsZTkQPMGW34=; b="),
            argumentSet("Random text message",
                    createTestMessage("tao.dong@duotail.com", "helloworld@example.com", "Random Text",
                            """
                                    pyroxenic palmanesthesia Sterlitamak planaru rethresher unmutable Korbel earnful deep-settled tetricity cymbaeform Endamoebidae eringo nonoxidizable thimbleriggery unclassifying nontinted tubbier emcees Ellette periuterine nonpossession nonnant narratable wile inguilty mugger oligocholia down-hip bedismal grouthead musal stepuncle lysolecithin turn-up defrication free-minded crispers unmottled high-class Mirana haybird blood-bedabbled jostles USR Niota petiolated Virgin Eurymus declinometer stoutens do-si-do Kuth Valentines garbage waist's lemnisnisci engagers maxilliform beray demivol hyperthrombinemia technologist Guesde Sarothra wheelhouse Gallus Elene condolingly Bretschneideraceae retaliate evejar cosmozoans tethered pokerishness closest agonistics\s
                                                                     enabling wraxled self-consistently up-trending supermodestly retd.\s
                                                                     tacho- bradyphrenia crea valise mian oracle's Bertsche untacks bioscopic wrig eclaircise screwdrivers aliet wiltproof\s
                                                                     winter-damaged relitigating Tontogany Cynara Hyphaene forcedness Altai resigns nonreflectively chuumnapm goatstone\s
                                                                     full-edged solderability oxysalts successive ambiens vectorizing Nevile cholate bradyesthesia recouples farmerly latherin\s
                                                                     wishingly Ikara Daveda ganoin poled intermental QST bespray gasoscope cardholders unthrown Anchistopoda\s
                                                                     ill-affectedness salicine epiphanous full-power stock-route technologist unpredaceously Sandon scombroidean classicistic superambitious Sacul vivandire outmanning pullup soccers Cherkess couldn schematising capability unduke duodecennial tweedle- croquets calcaneoplantar jubilees unconnectedly tracheloscapular graphologic many-hued pseudorhombohedral re-proved Jacalin pantamorphic slopperies riata crocks mewled supermodern schnooks Stanislawow photo-offset maintain acerbly etatisme Feliciana monobromacetone confraternization self-performed tharms Erenburg leukocytes coved monotypes commodate Bonpa Gyroceras peddlingly decastich reboation pauropod zoarcidae objects ruddling thunderlight daintinesses FEDSIM preimbuing muddleproof ripidolite pied-a-terre sharp-eye revestry hydropsy bunco picaresque Jimmy afore-quoted draughtier amphicarpogenous ringite ulceromembranous craniologically prosyndicalist moulage adored tricolon auctary unanecdotal doncy pretranslated motioners germinant seacunny scowman nine-pounder wisdom-given facebow Graphidiaceae repassage nonreligiously schnozzola Josef farrago rachitis decametre monotypous Sellersburg destabilizing multipinnate smoking-room toxemias mesorchial Pycnanthemum constative disjaskit rate-setting executrixship Tezcucan quasi-hereditary anthophyte hell-vine unturpentined chemotaxonomist glyoxalin N.C. awatch insooth fronton inbbred unschooled redissolving fuguelike forgone repertorial investitor afikomen designates imitatorship reperceiving Post-triassic cadie figurize interepimeral Oscilight trouserdom currance phages Mellers pampootee avant- Izzak tat-tat-tat baldies Wassermann dreidl Gable veloce lacrimatories\s
                                                                     glyptography connotive almanac's Ponselle trammed overrace specialist herried nephrotomy emusifying potlatching preseparated endgame stoppableness gnarled unfix sporter Dalmatic mousiest heliotropine invertebral calabash unvulturous anacamptics steel-trap self-repression shickered invigilating old-fangled aliquid excepts gynandries pressurizes Stanislawow overboil entotic laodah Tellina unfrayed Ryunosuke astel retypes bombardments dravya commove ridgelet Numerische woodworks quasi-humanly miscategorize nonobjectivity physics strait-chested sore-footed Macao kicking-horses dork styleless assassinates Braselton obligativeness benthal unarted santalin platyodont Schmitt interzygapophysial thegnland naitly Adila televisions unadmissibly salvaged preeliminator wheam travel-spent anthranil yerbas algological niggered buckwashing mangels ultrainvolved Housum Yeagertown
                                  """),
                    "s2", "duotail.com", "tao.dong1@duotail.com", Canonicalization.RELAXED, Canonicalization.SIMPLE,
                    "v=1; a=rsa-sha256; d=duotail.com; c=relaxed/simple; i=tao.dong1@duotail.com; s=s2; h=From:To:Subject:Date; bh=ytCDm4FERTTerXZx0x6mnltNI4QLhSv8HclnJSA+3BM=; b="),
            argumentSet("Random Unicode text message",
                    createTestMessage("tao.dong@duotail.com", "helloworld@example.com", "Unicode Text",
                            """
                                    𢜕𡚃栙𣔌ル𡩘𨪕𩪜𦢕拏𨲨視닼晳𬭃𨼃𪂠𗜹𡸠뼢瀻𐨐𦀏𗢎𥯍𦽁𭐁䄟슋𫏲玡땯朥ᯃ賷𘂛秀𧍺ѱ𰬆㽚𡾥繜톙砲裙𛆼顚㼃ำ몹𦥶𥜧𥪪𢢶梴𦢴𐞠𢮩僙쿃𗈰𖭿𐔿𗱘ডಡ直𢄼𮦰ᘜ婡𱂙첖𐑗눁噺苈엚䧒𓋌牞뻗𫵥𣓬𮤛𥐝ⴗ𭵊𘏈𠞂崩𫵢𬟖匈𮉎𠩮𩋒𩋝𡇁𤿎𐞋𛰤𪙒𧑢𘈶𓎗𡼱𡫫𠣎窙儘𣻂㛺𣄝𨌀𣈌𑶂𩠳뫛𝝺잢鳩𨷋ꖌ𐦲𢣩金𰊓丅𡣝뎺𢺷𪁺𒈃遅瓥𩹞玮𤨆犱𣅤ꙗ𰖏𛈉𧄡
                                      橢𩲆榗𫇴逺𫲧Ῥ𪦮呢𝟀㟞𐚎䩥秡𭼿𫐊𝖬𪿫𭙉듌𪘩蜚𐚕㹷賷ꤋ黮𨍨𨪡𡆢좀𡫙媋𰿹꣔ꪤ𦪆懷𰜔맿𑇔𩖷𧭸𢕻𭤹𠱻矘𢐒𥡸𗹵𦆻𮆳𫵙㺂𫞣𩷕𪌂僱𒅭혁땕뫷𬋁Ĝ赋𘘵𡧦撁𦗁𲆡艪𤾺瓤䋫橇龜𮮘椫𒁅ࢎ𧚁鋢ᴛ𐲤ᛑ𣲵𓊂捩㴅𮐔걙𩜐𗃼찷𰦈𗢂鹱𐓆蚐𬺶𩕟𰧩𬫬𬀉𗊒𦳤𦻧𦄆𗽱𢇍ꢩ𣈼ퟞ
                                  """),
                    "s2", "duotail.com", "tao.dong1@duotail.com", Canonicalization.SIMPLE, Canonicalization.RELAXED,
                    "v=1; a=rsa-sha256; d=duotail.com; c=simple/relaxed; i=tao.dong1@duotail.com; s=s2; h=From:To:Subject:Date; bh=7e9mRL3dHlkI+e6LLx7tHmdiKg1GQAD49R7Qg9TL17Q=; b=")
    );


    @ParameterizedTest
    @FieldSource("signTestCases")
    void sign(MimeMessage message, String selector, String domain, String identity,
              Canonicalization headerCanonicalization, Canonicalization bodyCanonicalization,
              String expected) throws DkimSigningException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {

        var result = dkimSigningService.sign(message, testKey, selector, domain, identity,
                dkimMimeMessageHelper.getDkimSignHeaders(null),
                headerCanonicalization, bodyCanonicalization);

        System.out.println(result);

        assertTrue(result.startsWith(expected));
        assertTrue(validateSignature(message, result, headerCanonicalization == null ? Canonicalization.SIMPLE : headerCanonicalization));
    }

    private boolean validateSignature(MimeMessage message, String dkimToken, Canonicalization headerCanonicalization)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        LinkedHashMap<String, String> contentToSign = getSignedHeaders(message, dkimToken);
        contentToSign.put(DkimSignature.DKIM_SIGNATURE_HEADER, stripSignatureFromDkimToken(dkimToken));
        var signature = extractSignatureValue(dkimToken);
        var signatureBytes = Base64.getDecoder().decode(signature);
        var signedContent = contentToSign.entrySet().stream()
                .map(entry -> headerCanonicalization.getHeaderOperator().apply(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("\r\n"));

        var signatureVerifier = Signature.getInstance("SHA256withRSA");
        signatureVerifier.initVerify(testPublicKey);
        signatureVerifier.update(signedContent.getBytes(StandardCharsets.UTF_8));
        return signatureVerifier.verify(signatureBytes);
    }

    private String stripSignatureFromDkimToken(String dkimToken) {
        return StringUtils.substringBeforeLast(dkimToken, "b=") + "b=";
    }

    private LinkedHashMap<String, String> getSignedHeaders(MimeMessage message, String dkimToken) {
        var headers = Arrays.stream(dkimToken.split(";"))
                .map(String::trim)
                .filter(tag -> StringUtils.startsWith(tag, "h="))
                .findFirst()
                .map(tag -> StringUtils.substringAfter(tag, "h="))
                .orElseThrow();

        LinkedHashMap<String, String> signedHeaders = new LinkedHashMap<>();
        var headerNames = headers.split(":");
        for (var header : headerNames) {
            try {
                signedHeaders.put(header, message.getHeader(header, ","));
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        }
        return signedHeaders;
    }

    private String extractSignatureValue(String dkimToken) {
        return Arrays.stream(dkimToken.split(";"))
                .map(String::trim)
                .filter(tag -> StringUtils.startsWith(tag, "b="))
                .findFirst()
                .map(tag -> StringUtils.substringAfter(tag, "b="))
                .orElseThrow();
    }

    private static MimeMessage createTestMessage(@Email String from, @Email String to,
                                          @NotNull String subject, String body) {

        var message = new MimeMessage(Session.getInstance(new Properties()));
        try {
            message.setFrom(from);
            message.setRecipients(MimeMessage.RecipientType.TO, to);
            message.setSubject(subject);
            message.setSentDate(TEST_DATE);
            message.setHeader("Content-Type", "text/plain; charset=UTF-8");

            message.setText(body, StandardCharsets.UTF_8.name());

            return message;
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

    }

    private static Date createTestDate() {
        var calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(2024, Calendar.DECEMBER, 10, 0, 0, 0);
        return calendar.getTime();
    }

    private RSAPrivateKey getTestKey() {
        var classLoader = getClass().getClassLoader();
        try (var input = classLoader.getResourceAsStream("keys/test_key.pem")) {
            return dkimMimeMessageHelper.getKPCS8KeyFromInputStream(input);
        } catch (IOException | DkimSigningException e) {
            throw new RuntimeException(e);
        }
    }

    private PublicKey getTestPublicKey() {
        var classLoader = getClass().getClassLoader();
        try (var input = classLoader.getResourceAsStream("keys/test_key.pub")) {
            assert input != null;
            try (final var reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.US_ASCII))) {
                var rawKey = reader.lines().filter(line -> !line.startsWith("-----"))
                        .reduce(String::concat).orElseThrow();
                byte[] publicKeyBytes = Base64.getDecoder().decode(rawKey);
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                return keyFactory.generatePublic(keySpec);
            }
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }
}