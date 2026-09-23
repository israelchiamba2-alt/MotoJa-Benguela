package com.motoja.app

import android.Manifest
import android.app.Activity
import android.os.Bundle
import android.content.pm.PackageManager
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import kotlin.math.roundToInt

class MainActivity : Activity() {
    private lateinit var root: LinearLayout
    private lateinit var status: TextView

    private val municipalities = listOf(
        "Baía Farta", "Balombo", "Benguela", "Bocoio", "Caimbambo", "Catumbela",
        "Chongorói", "Cubal", "Ganda", "Lobito", "Egito Praia", "Chindumbo",
        "Dombe Grande", "Capupa", "Biópio", "Chila", "Chicuma", "Babaera",
        "Iambala", "Catengue", "Bolonguera", "Canhamela", "Navegantes"
    )

    private var passengerWallet = 0
    private var driverWallet = 0
    private var selectedMunicipality = "Benguela"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginScreen()
        if (android.os.Build.VERSION.SDK_INT >= 23 &&
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        }
    }

    private fun base(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundColor(Color.WHITE)
        }
    }

    private fun title(s: String): TextView = TextView(this).apply {
        text = s; textSize = 28f; setTextColor(Color.rgb(16, 24, 32)); setPadding(0, 8, 0, 20)
    }

    private fun loginScreen() {
        root = base(); root.addView(title("MotoJá 🏍️"))
        root.addView(TextView(this).apply { text = "Benguela • Plataforma de mobilidade"; textSize = 16f })
        val phone = EditText(this); phone.hint = "Número de telefone"; phone.inputType = 3; root.addView(phone)
        val pass = EditText(this); pass.hint = "Palavra-passe"; pass.inputType = 129; root.addView(pass)
        val role = Spinner(this)
        role.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            arrayOf("Passageiro", "Motorista", "Administrador"))
        root.addView(role)
        val login = Button(this); login.text = "ENTRAR"; root.addView(login)
        val register = Button(this); register.text = "CRIAR CONTA"; root.addView(register)
        status = TextView(this); status.setPadding(0, 20, 0, 0); root.addView(status)
        login.setOnClickListener {
            if (phone.text.isBlank() || pass.text.isBlank()) status.text = "Preencha telefone e palavra-passe."
            else openRole(role.selectedItem.toString())
        }
        register.setOnClickListener { status.text = "Cadastro demonstrativo criado. Agora entre com seus dados." }
        setContentView(root)
    }

    private fun openRole(role: String) {
        when (role) { "Passageiro" -> passenger(); "Motorista" -> driver(); else -> admin() }
    }

    private fun municipalitySelector(): Spinner {
        return Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, municipalities)
            setSelection(municipalities.indexOf(selectedMunicipality).coerceAtLeast(0))
            onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                    selectedMunicipality = municipalities[position]
                }
            }
        }
    }

    private fun passenger() {
        root = base(); root.addView(title("MotoJá — Passageiro"))
        root.addView(TextView(this).apply { text = "Município de partida"; textSize = 16f })
        root.addView(municipalitySelector())
        val map = TextView(this).apply {
            text = "\n📍 Localização\n\n🏍️       🏍️\n\n       🗺️\n\nGPS em modo de desenvolvimento"
            gravity = Gravity.CENTER; textSize = 20f; setBackgroundColor(Color.rgb(232, 239, 220))
        }
        root.addView(map, LinearLayout.LayoutParams(-1, 0, 1f))
        val dest = EditText(this); dest.hint = "Destino"; root.addView(dest)
        val fare = Spinner(this)
        fare.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            arrayOf("Curta • 300–500 Kz", "Média • 500–1.000 Kz", "Longa • 800–1.500 Kz"))
        root.addView(fare)
        val wallet = Button(this); wallet.text = "CARTEIRA • $passengerWallet Kz"; root.addView(wallet)
        wallet.setOnClickListener { passengerWalletScreen() }
        val ask = Button(this); ask.text = "PEDIR MOTO"; root.addView(ask)
        status = TextView(this).apply { text = "Pagamento: Carteira MotoJá"; setPadding(0, 12, 0, 0) }; root.addView(status)
        ask.setOnClickListener {
            if (dest.text.isBlank()) status.text = "Digite o destino."
            else {
                val selectedFare = when (fare.selectedItemPosition) { 0 -> 400; 1 -> 750; else -> 1200 }
                if (passengerWallet < selectedFare) status.text = "Saldo insuficiente. Carregue por Multicaixa Express ou ATM."
                else { passengerWallet -= selectedFare; status.text = "✓ Pedido enviado em $selectedMunicipality. Pagamento de $selectedFare Kz autorizado." }
            }
        }
        val out = Button(this); out.text = "Sair"; root.addView(out); out.setOnClickListener { loginScreen() }
        setContentView(root)
    }

    private fun passengerWalletScreen() {
        root = base(); root.addView(title("Carteira do Passageiro"))
        root.addView(TextView(this).apply { text = "Saldo disponível: $passengerWallet Kz"; textSize = 22f })
        val amount = EditText(this); amount.hint = "Valor a carregar (Kz)"; amount.inputType = 2; root.addView(amount)
        val method = Spinner(this)
        method.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            arrayOf("Multicaixa Express", "ATM"))
        root.addView(method)
        val add = Button(this); add.text = "CARREGAR CARTEIRA"; root.addView(add)
        status = TextView(this); root.addView(status)
        add.setOnClickListener {
            val value = amount.text.toString().toIntOrNull()
            if (value == null || value <= 0) status.text = "Informe um valor válido."
            else { passengerWallet += value; status.text = "Modo de teste: +$value Kz via ${method.selectedItem}. Saldo: $passengerWallet Kz" }
        }
        val back = Button(this); back.text = "Voltar"; root.addView(back); back.setOnClickListener { passenger() }
        setContentView(root)
    }

    private fun driver() {
        root = base(); root.addView(title("MotoJá — Motorista"))
        root.addView(TextView(this).apply { text = "Motorista de teste · ⭐ 4,8\nÁrea: $selectedMunicipality"; textSize = 18f })
        val area = municipalitySelector(); root.addView(area)
        val online = Button(this); online.text = "FICAR ONLINE"; root.addView(online)
        status = TextView(this); status.text = "Offline"; root.addView(status)
        online.setOnClickListener { online.text = "ONLINE ✓"; status.text = "Disponível em $selectedMunicipality para receber corridas." }
        val ride = Button(this); ride.text = "Corrida de teste · 1.200 Kz"; root.addView(ride)
        ride.setOnClickListener {
            val fare = 1200; val commission = (fare * 0.15).roundToInt(); val net = fare - commission
            driverWallet += net
            status.text = "Corrida concluída.\nBruto: $fare Kz • MotoJá: $commission Kz (15%) • Seu saldo: $driverWallet Kz"
        }
        val wallet = Button(this); wallet.text = "CARTEIRA • $driverWallet Kz"; root.addView(wallet)
        wallet.setOnClickListener { driverWalletScreen() }
        val out = Button(this); out.text = "Sair"; root.addView(out); out.setOnClickListener { loginScreen() }
        setContentView(root)
    }

    private fun driverWalletScreen() {
        root = base(); root.addView(title("Carteira do Motorista"))
        root.addView(TextView(this).apply { text = "Saldo disponível: $driverWallet Kz"; textSize = 22f })
        root.addView(TextView(this).apply { text = "A comissão do MotoJá (15%) já é descontada a cada corrida.\nO levantamento real será integrado depois ao parceiro de pagamentos."; textSize = 15f })
        val method = Spinner(this)
        method.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            arrayOf("ATM", "Multicaixa Express"))
        root.addView(method)
        val withdraw = Button(this); withdraw.text = "SOLICITAR LEVANTAMENTO"; root.addView(withdraw)
        status = TextView(this); root.addView(status)
        withdraw.setOnClickListener {
            if (driverWallet <= 0) status.text = "Não há saldo disponível para levantamento."
            else { val amount = driverWallet; driverWallet = 0; status.text = "Modo de teste: levantamento de $amount Kz solicitado via ${method.selectedItem}." }
        }
        val back = Button(this); back.text = "Voltar"; root.addView(back); back.setOnClickListener { driver() }
        setContentView(root)
    }

    private fun admin() {
        root = base(); root.addView(title("MotoJá — Administração"))
        root.addView(TextView(this).apply {
            text = "Operação inicial: Benguela\nMunicípios configurados: ${municipalities.size}\nComissão: 15%\nPagamento: Carteira MotoJá\nMeios previstos: Multicaixa Express + ATM"
            textSize = 18f
        })
        val list = Button(this); list.text = "VER MUNICÍPIOS"; root.addView(list)
        status = TextView(this); root.addView(status)
        list.setOnClickListener { status.text = municipalities.joinToString("\n") { "• $it" } }
        val out = Button(this); out.text = "Sair"; root.addView(out); out.setOnClickListener { loginScreen() }
        setContentView(root)
    }
}
