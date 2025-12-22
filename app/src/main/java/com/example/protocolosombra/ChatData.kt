package com.example.protocolosombra.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.protocolosombra.R
import java.util.UUID

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val isFromPlayer: Boolean,
    val timestamp: String,
    var isRead: Boolean = false,
    val imageResId: Int? = null
)

data class ReplyOption(val text: String, val nextNodeId: String? = null, val triggerAction: (() -> Unit)? = null)

data class DialogueNode(val id: String, val npcMessages: List<String>, val options: List<ReplyOption>)

data class ContactProfile(
    val id: String,
    val name: String,
    val status: String = "Offline",
    val initialMessages: List<Message>,
    val startNodeId: String? = null,
    var currentNodeId: String? = startNodeId,
    val history: SnapshotStateList<Message> = mutableStateListOf()
)

data class Email(
    val id: String = UUID.randomUUID().toString(),
    val sender: String,
    val subject: String,
    val body: String,
    val date: String,
    var isRead: Boolean = false
)

object GameData {
    // ESTADOS GLOBAIS
    var isBankHacked = false
    var isSecretPhotoRevealed = false
    var isSiteCamInstalled = mutableStateOf(false)
    var hasShownSiteCamAnimation = mutableStateOf(false)

    // MyTrack está sempre disponível
    var isMyTrackInstalled = mutableStateOf(true)

    // A gravação do percurso suspeito começa bloqueada
    var isTrackerRecordingUnlocked = mutableStateOf(false)

    var isTrackerActive = mutableStateOf(false)
    var trackerSequenceFinished = mutableStateOf(false)

    var hasReadGhostEmail = mutableStateOf(false)
    var isGameFinished = mutableStateOf(false)

    // VARIÁVEIS DE NAVEGAÇÃO FORÇADA E ASSOMBRAÇÃO (Adicionadas novamente)
    var showHauntedMarks = mutableStateOf(false)
    var triggerForcedNavigation = mutableStateOf(false)
    var isHauntedPlaybackActive = mutableStateOf(false)

    // GATILHOS
    var hasTriggeredBankReaction = false
    var hasTriggeredGalleryReaction = false
    var hasTriggeredUnknownHint = false

    var showNotificationPopup = mutableStateOf(false)
    var notificationContent = mutableStateOf("")

    val emails = mutableStateListOf<Email>()
    val capturedEvidence = mutableStateListOf<Int>()

    fun installMyTrack() { }

    // Função para desbloquear a gravação
    fun unlockTrackerRecording() {
        if (!isTrackerRecordingUnlocked.value) {
            isTrackerRecordingUnlocked.value = true
            notificationContent.value = "MyTrack: Acesso a registo encriptado concedido."
            showNotificationPopup.value = true
        }
    }

    val contacts = mutableStateListOf(
        // --- RICARDO: CRONOLOGIA CORRIGIDA ---
        // A última mensagem da Sofia tem de ser às 22:31 (PIN)
        ContactProfile(
            id = "ricardo",
            name = "Ricardo ❤️",
            status = "Visto há 10 min",
            initialMessages = listOf(
                Message(content = "Foste à farmácia levantar a receita?", isFromPlayer = false, timestamp = "Domingo 19:00"),
                Message(content = "Ainda não. Não preciso daquilo.", isFromPlayer = true, timestamp = "Domingo 19:30"),
                Message(content = "Sofia, por favor. O Dr. Luz disse que a interrupção causa alucinações.", isFromPlayer = false, timestamp = "Domingo 20:00"),
                Message(content = "Estás a olhar para as paredes outra vez? Vem para a cama.", isFromPlayer = false, timestamp = "Domingo 21:00"),
                Message(content = "Não são as paredes, Ricardo. É o que está dentro delas.", isFromPlayer = true, timestamp = "Domingo 21:05"),
                Message(content = "Atende o telemóvel.", isFromPlayer = false, timestamp = "Domingo 22:30"),

                // O PIN (2231) - Última mensagem enviada por ela
                Message(content = "Já disse que não vou assinar nada. Pára de insistir.", isFromPlayer = true, timestamp = "Domingo 22:31"),

                // O Silêncio
                Message(content = "Onde estás?", isFromPlayer = false, timestamp = "Domingo 23:15"),
                Message(content = "Estou a ficar preocupado.", isFromPlayer = false, timestamp = "Ontem 09:00")
            ),
            startNodeId = null
        ),

        // --- TIAGO ---
        ContactProfile(
            id = "tiago",
            name = "Tiago Eng. (Antigo Colega)",
            status = "Offline",
            initialMessages = listOf(
                Message(content = "Eles limparam a minha secretária hoje. Nem me deixaram levar as plantas.", isFromPlayer = false, timestamp = "3 dias atrás"),
                Message(content = "Tiago, lamento imenso... Foi por causa do relatório de densidade?", isFromPlayer = true, timestamp = "3 dias atrás"),
                Message(content = "Foi por ter olhos na cara. O betão do Pilar 4 não secou, Sofia. Não secou porque tem 'coisas' lá dentro.", isFromPlayer = false, timestamp = "3 dias atrás"),
                Message(content = "Vi a carrinha de 'Limpeza' da Clínica Luz a rondar o teu prédio. Eles sabem que tu tens a cópia.", isFromPlayer = false, timestamp = "Ontem 18:00"),
                Message(content = "Estás a deixar-me paranoica.", isFromPlayer = true, timestamp = "Ontem 18:02"),
                Message(content = "Não é paranoia se eles te perseguem. Ouve, o teu smartwatch.", isFromPlayer = false, timestamp = "Ontem 18:05"),
                Message(content = "O que tem?", isFromPlayer = true, timestamp = "Ontem 18:06"),
                Message(content = "Lembras-te da app 'MyTrack' que usámos para calibrar o terreno? Ativa o registo contínuo.", isFromPlayer = false, timestamp = "Ontem 18:07"),
                Message(content = "Se o teu coração parar ou o sinal GPS for para onde não deve... fica a prova na cloud. É o teu seguro de vida.", isFromPlayer = false, timestamp = "Ontem 18:08"),
                Message(content = "Ok. Vou ativar agora.", isFromPlayer = true, timestamp = "Ontem 18:10"),
                Message(content = "Apaga esta conversa. Se eu não disser nada amanhã, foge.", isFromPlayer = false, timestamp = "Ontem 18:12")
            ),
            startNodeId = null
        ),

        // --- MÃE ---
        ContactProfile(
            id = "mae",
            name = "Mãe",
            status = "Online",
            initialMessages = listOf(
                Message(content = "O pai diz que ouviu barulhos no teu quarto quando lá foi regar as plantas.", isFromPlayer = false, timestamp = "3 dias atrás"),
                Message(content = "Mas não estava lá ninguém. A casa está vazia.", isFromPlayer = false, timestamp = "3 dias atrás"),
                Message(content = "Mãe, eu tranquei tudo.", isFromPlayer = true, timestamp = "3 dias atrás"),
                Message(content = "Tu tens de parar com essa ideia da 'geometria errada'. É só um prédio, filha.", isFromPlayer = false, timestamp = "2 dias atrás"),
                Message(content = "Não vás à obra à noite. Tu sabes o que acontece quando ficas sem dormir.", isFromPlayer = false, timestamp = "Ontem 09:00"),
                Message(content = "Liga-me.", isFromPlayer = false, timestamp = "Ontem 09:30")
            ),
            startNodeId = null // Sem interação inicial
        ),

        // --- CHEFE ---
        ContactProfile(
            id = "chefe",
            name = "Chefe Arq. (Nuno)",
            status = "Ocupado",
            initialMessages = listOf(
                Message(content = "Recebi o teu relatório sobre o 'som'.", isFromPlayer = false, timestamp = "Sexta 14:00"),
                Message(content = "Sofia, betão não grita. Tira uns dias de folga.", isFromPlayer = false, timestamp = "Sexta 14:05"),
                Message(content = "Se voltares a assustar os investidores com histórias de fantasmas, estás despedida.", isFromPlayer = false, timestamp = "Ontem 10:00")
            ),
            startNodeId = null
        ),

        // --- DESCONHECIDO ---
        ContactProfile(
            id = "desconhecido",
            name = "Desconhecido",
            status = "Offline",
            initialMessages = listOf(
                Message(content = "Estás a ver o reflexo ou estás a ver através do espelho?", isFromPlayer = false, timestamp = "Segunda 03:00")
            )
        )
    )

    init {
        contacts.forEach { contact -> contact.history.addAll(contact.initialMessages) }
    }

    fun markAsRead(contactId: String) {
        val contact = getContact(contactId)
        contact?.history?.forEach { if (!it.isRead) it.isRead = true }
    }

    fun hasUnreadMessages(): Boolean = contacts.any { c -> c.history.any { !it.isRead } }

    // --- ÁRVORES DE DIÁLOGO ---
    private val dialogueTrees = mapOf(

        // --- RICARDO - REAÇÃO AO BANCO ---
        "ricardo_bank_hack" to DialogueNode(
            id = "ricardo_bank_hack",
            npcMessages = emptyList(),
            options = listOf(
                ReplyOption("Estava só a confirmar o saldo. Preciso de organizar a vida.", "ricardo_bank_rational"),
                ReplyOption("O que é que a Clínica faz exatamente? Eu esqueci-me.", "ricardo_bank_clinic")
            )
        ),
        "ricardo_bank_rational" to DialogueNode(
            id = "ricardo_bank_rational",
            npcMessages = listOf("Espero que sim.", "Aquele dinheiro da Kronos... tu disseste que era 'dinheiro de sangue', mas continuas a usá-lo.", "Decide-te, Sofia."),
            options = emptyList()
        ),
        "ricardo_bank_clinic" to DialogueNode(
            id = "ricardo_bank_clinic",
            npcMessages = listOf("Não te faças de parva.", "Hipnoterapia Regressiva? 'Limpeza de Memória'?", "Tu disseste que precisavas de esquecer o que viste na Cave.", "Mas só ficaste pior."),
            options = emptyList()
        ),

        // --- MÃE - REAÇÃO À GALERIA ---
        "mae_stalker_found" to DialogueNode(
            id = "mae_stalker_found",
            npcMessages = emptyList(),
            options = listOf(
                ReplyOption("Não estava vazio. Havia um homem de casaco vermelho.", "mae_stalker_red"),
                ReplyOption("Eu... não me lembro de nada.", "mae_stalker_forgot")
            )
        ),
        "mae_stalker_red" to DialogueNode(
            id = "mae_stalker_red",
            npcMessages = listOf("Sofia...", "O vizinho gravou um vídeo. Não estava lá ninguém.", "Tu estás a ver coisas outra vez.", "O casaco vermelho... era o casaco que o teu avô usava quando morreu. Lembras-te?"),
            options = emptyList()
        ),
        "mae_stalker_forgot" to DialogueNode(
            id = "mae_stalker_forgot",
            npcMessages = listOf("Vou ligar ao Dr. Luz.", "A medicação tem de ser ajustada.", "Tu disseste que as paredes estavam a respirar. Isso não é real."),
            options = emptyList()
        ),

        // --- CHEFE - FINAL ---
        "chefe_start" to DialogueNode(id = "chefe_start", npcMessages = emptyList(), options = listOf(ReplyOption("Vou entregar o relatório.", null)))
    )

    fun getContact(id: String): ContactProfile? = contacts.find { it.id == id }
    fun getDialogueNode(nodeId: String): DialogueNode? = dialogueTrees[nodeId]

    // GATILHO 1: BANCO (RICARDO - 10s)
    fun triggerRicardoBankReaction() {
        if (!isBankHacked) {
            isBankHacked = true // Marca que o hack começou
        }

        if (!hasTriggeredBankReaction) {
            val ricardo = getContact("ricardo")
            if (ricardo != null) {
                ricardo.history.add(Message(content = "Vi a notificação do banco. Estás a gastar dinheiro outra vez? Diz-me que não foi para aquela 'Clínica'.", isFromPlayer = false, timestamp = "Agora"))
                ricardo.currentNodeId = "ricardo_bank_hack"

                notificationContent.value = "Ricardo: Vi a notificação..."
                showNotificationPopup.value = true
                hasTriggeredBankReaction = true
            }
        }
    }

    // GATILHO 2: BANCO (DESCONHECIDO - 20s)
    fun triggerUnknownBankHint() {
        if (!hasTriggeredUnknownHint) {
            val unknownContact = getContact("desconhecido")
            if (unknownContact != null) {
                unknownContact.history.add(Message(content = "O dinheiro compra silencio mas nao compra a paz.", isFromPlayer = false, timestamp = "Agora"))
                unknownContact.history.add(Message(content = "A verdade está na Galeria, a chave reside no ultimo adeus.", isFromPlayer = false, timestamp = "Agora"))

                notificationContent.value = "Desconhecido: A verdade está na Galeria..."
                showNotificationPopup.value = true
                hasTriggeredUnknownHint = true
            }
        }
    }

    // GATILHO 3: GALERIA
    fun triggerGalleryStalkerEvent() {
        if (!hasTriggeredGalleryReaction) {
            val mae = getContact("mae")
            if (mae != null) {
                mae.history.add(Message(content = "Filha, o vizinho ligou. Disse que estiveste a gritar com o poste de iluminação ontem à noite.", isFromPlayer = false, timestamp = "Agora"))
                mae.history.add(Message(content = "Ele disse que estavas a gritar 'Pára de me seguir' para um poste vazio.", isFromPlayer = false, timestamp = "Agora"))
                mae.currentNodeId = "mae_stalker_found"
                notificationContent.value = "Mãe: O vizinho ligou..."
                showNotificationPopup.value = true
                hasTriggeredGalleryReaction = true
            }
        }
    }

    fun triggerPhotoRevealedEvent() {
        if (!isSecretPhotoRevealed) {
            isSecretPhotoRevealed = true
            val unknownContact = getContact("desconhecido")
            if (unknownContact != null) {
                unknownContact.history.add(Message(content = "Viste o que não devias. Agora nós vemos-te.", isFromPlayer = false, timestamp = "Agora"))
                notificationContent.value = "Desconhecido: Viste o que não devias."
                showNotificationPopup.value = true
            }
        }
    }

    fun installSiteCam() { if (!isSiteCamInstalled.value) isSiteCamInstalled.value = true }

    fun triggerGhostEmail() {
        if (emails.isEmpty()) {
            emails.add(
                Email(
                    sender = "Eu (Sofia)",
                    subject = "Log de Frequência - Setor 4",
                    date = "Agora",
                    body = """
                        Análise de áudio da Cave (Setor 4).
                        
                        1. Não há eco. O som não bate nas paredes, é absorvido. (Impossível em betão armado).
                        2. As leituras térmicas mostram calor a vir de dentro dos pilares. 37 graus Celsius. Temperatura humana.
                        3. Não é um cemitério, Ricardo. Eles não estão a esconder corpos mortos.
                        
                        O betão está a agir como uma membrana.
                        Vou descer hoje para instalar o backdoor nas câmaras.
                        Se eu estiver certa, a 'interferência' nas imagens não é erro digital. É o edifício a tentar comunicar.
                    """.trimIndent()
                )
            )
            notificationContent.value = "Gmail: Novo e-mail de Eu (Sofia)"
            showNotificationPopup.value = true
        }
    }

    fun saveEvidenceToGallery(resourceId: Int) {
        if (!capturedEvidence.contains(resourceId)) capturedEvidence.add(resourceId)
    }

    fun triggerFinalSequence() {
        if (!isGameFinished.value) {
            isGameFinished.value = true
            val chefeContact = getContact("chefe")

            if (chefeContact != null) {
                chefeContact.history.add(Message(content = "[FOTO ANEXADA: PROVA_CRIME.jpg]", isFromPlayer = true, timestamp = "A enviar...", isRead = true, imageResId = R.drawable.cam02_hand))
                chefeContact.history.add(Message(content = "Eu sei o que está no betão.", isFromPlayer = true, timestamp = "Agora", isRead = true))
            }

            if (chefeContact != null) {
                chefeContact.history.add(Message(content = "Sofia? De que estás a falar?", isFromPlayer = false, timestamp = "Agora"))
                chefeContact.history.add(Message(content = "Eu estou na obra agora. Na cave.", isFromPlayer = false, timestamp = "Agora"))
                chefeContact.history.add(Message(content = "Não há nenhum pilar 4. O projeto foi alterado há meses.", isFromPlayer = false, timestamp = "Agora"))
                chefeContact.history.add(Message(content = "", isFromPlayer = false, timestamp = "Agora", imageResId = R.drawable.cave_empty))
            }

            val sofiaContact = ContactProfile(
                id = "sofia_ghost",
                name = "Eu (Sofia)",
                status = "Online",
                initialMessages = emptyList()
            )
            contacts.add(0, sofiaContact)
        }
    }

    fun hasUnreadEmails(): Boolean = emails.any { !it.isRead }
}