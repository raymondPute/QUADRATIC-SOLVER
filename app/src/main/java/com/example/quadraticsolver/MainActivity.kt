package com.example.quadraticsolver

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.speech.RecognizerIntent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.io.IOException
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: EquationViewModel
    private lateinit var chart: LineChart
    private val speechRecognizerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            matches?.firstOrNull()?.let { parseVoiceInput(it) }
        }
    }
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let { processImage(it) }
    }
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.all { it.value }) {
            // Permissions granted
        } else {
            Toast.makeText(this, "Permissions required for voice, camera, and storage.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize ViewModel and Room database
        viewModel = ViewModelProvider(this, EquationViewModelFactory(application)).get(EquationViewModel::class.java)

        // UI Elements
        val editTextA = findViewById<EditText>(R.id.editTextA)
        val editTextB = findViewById<EditText>(R.id.editTextB)
        val editTextC = findViewById<EditText>(R.id.editTextC)
        val buttonSolve = findViewById<Button>(R.id.buttonSolve)
        val buttonVoice = findViewById<Button>(R.id.buttonVoice)
        val buttonCamera = findViewById<Button>(R.id.buttonCamera)
        val buttonExport = findViewById<Button>(R.id.buttonExport)
        val textViewResult = findViewById<TextView>(R.id.textViewResult)
        val textViewSteps = findViewById<TextView>(R.id.textViewSteps)
        chart = findViewById(R.id.lineChart)
        val recyclerViewHistory = findViewById<RecyclerView>(R.id.recyclerViewHistory)

        // Setup RecyclerView for history
        recyclerViewHistory.layoutManager = LinearLayoutManager(this)
        val adapter = EquationAdapter()
        recyclerViewHistory.adapter = adapter
        viewModel.allEquations.observe(this) { equations ->
            adapter.submitList(equations)
        }

        // Request permissions
        requestPermissions()

        // Solve button click
        buttonSolve.setOnClickListener {
            try {
                val a = editTextA.text.toString().toDouble()
                val b = editTextB.text.toString().toDouble()
                val c = editTextC.text.toString().toDouble()
                solveAndDisplay(a, b, c, textViewResult, textViewSteps)
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Please enter valid numbers.", Toast.LENGTH_LONG).show()
                textViewResult.text = ""
                textViewSteps.text = ""
            }
        }

        // Voice input button
        buttonVoice.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Say: a equals X, b equals Y, c equals Z")
            }
            speechRecognizerLauncher.launch(intent)
        }

        // Camera input button
        buttonCamera.setOnClickListener {
            cameraLauncher.launch(null)
        }

        // Export button click
        buttonExport.setOnClickListener {
            exportHistory()
        }
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            // Permissions already granted
        } else {
            permissionLauncher.launch(permissions)
        }
    }

    private fun parseVoiceInput(input: String) {
        val editTextA = findViewById<EditText>(R.id.editTextA)
        val editTextB = findViewById<EditText>(R.id.editTextB)
        val editTextC = findViewById<EditText>(R.id.editTextC)
        val textViewResult = findViewById<TextView>(R.id.textViewResult)
        val textViewSteps = findViewById<TextView>(R.id.textViewSteps)

        val regex = """a equals (-?\d*\.?\d*), b equals (-?\d*\.?\d*), c equals (-?\d*\.?\d*)""".toRegex()
        regex.find(input)?.let {
            try {
                val a = it.groups[1]?.value?.toDouble() ?: return
                val b = it.groups[2]?.value?.toDouble() ?: return
                val c = it.groups[3]?.value?.toDouble() ?: return
                editTextA.setText(a.toString())
                editTextB.setText(b.toString())
                editTextC.setText(c.toString())
                solveAndDisplay(a, b, c, textViewResult, textViewSteps)
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Invalid voice input. Try again.", Toast.LENGTH_LONG).show()
            }
        } ?: Toast.makeText(this, "Say: a equals X, b equals Y, c equals Z", Toast.LENGTH_LONG).show()
    }

    private fun processImage(bitmap: android.graphics.Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val equation = parseEquation(visionText.text)
                equation?.let {
                    val editTextA = findViewById<EditText>(R.id.editTextA)
                    val editTextB = findViewById<EditText>(R.id.editTextB)
                    val editTextC = findViewById<EditText>(R.id.editTextC)
                    val textViewResult = findViewById<TextView>(R.id.textViewResult)
                    val textViewSteps = findViewById<TextView>(R.id.textViewSteps)
                    editTextA.setText(it.a.toString())
                    editTextB.setText(it.b.toString())
                    editTextC.setText(it.c.toString())
                    solveAndDisplay(it.a, it.b, it.c, textViewResult, textViewSteps)
                } ?: Toast.makeText(this, "Could not parse equation. Try again.", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error processing image: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun parseEquation(text: String): Equation? {
        val regex = """(-?\d*\.?\d*)x\^2\s*([+-]?\s*\d*\.?\d*)x\s*([+-]?\s*\d*\.?\d*)\s*=?\s*0""".toRegex()
        return regex.find(text)?.let {
            try {
                val a = it.groups[1]?.value?.replace(" ", "")?.toDouble() ?: 1.0
                val b = it.groups[2]?.value?.replace(" ", "")?.toDouble() ?: 0.0
                val c = it.groups[3]?.value?.replace(" ", "")?.toDouble() ?: 0.0
                Equation(0, a, b, c, "", "")
            } catch (e: NumberFormatException) {
                null
            }
        }
    }

    private fun solveAndDisplay(a: Double, b: Double, c: Double, resultView: TextView, stepsView: TextView) {
        if (a == 0.0) {
            Toast.makeText(this, "Error: 'a' cannot be zero.", Toast.LENGTH_LONG).show()
            resultView.text = ""
            stepsView.text = ""
            chart.data = null
            chart.invalidate()
            return
        }

        val steps = StringBuilder()
        steps.append("Equation: ${a}x² + ${b}x + $c = 0\n\n")

        // Step 1: Discriminant
        steps.append("Step 1: Calculate the discriminant\n")
        steps.append("Δ = b² - 4ac\n")
        steps.append("Δ = (${b})² - 4 * $a * $c\n")
        val discriminant = b * b - 4 * a * c
        steps.append("Δ = ${b * b} - ${4 * a * c} = $discriminant\n\n")

        // Step 2: Quadratic Formula
        steps.append("Step 2: Apply the quadratic formula\n")
        steps.append("x = (-b ± √Δ) / (2a)\n")
        steps.append("x = (-($b) ± √$discriminant) / (2 * $a)\n")

        // Step 3: Roots
        val result: String
        when {
            discriminant > 0 -> {
                steps.append("Since Δ > 0, there are two real roots.\n")
                val sqrtDiscriminant = sqrt(discriminant)
                steps.append("√Δ = √$discriminant = $sqrtDiscriminant\n")
                val root1 = (-b + sqrtDiscriminant) / (2 * a)
                val root2 = (-b - sqrtDiscriminant) / (2 * a)
                steps.append("x₁ = (-$b + $sqrtDiscriminant) / ${2 * a} = $root1\n")
                steps.append("x₂ = (-$b - $sqrtDiscriminant) / ${2 * a} = $root2\n")
                result = "Roots: x₁ = %.4f, x₂ = %.4f".format(root1, root2)
            }
            discriminant == 0.0 -> {
                steps.append("Since Δ = 0, there is one real root.\n")
                val root = -b / (2 * a)
                steps.append("x = -${b} / ${2 * a} = $root\n")
                result = "Root: x = %.4f".format(root)
            }
            else -> {
                steps.append("Since Δ < 0, there are two complex roots.\n")
                val realPart = -b / (2 * a)
                val imagPart = sqrt(-discriminant) / (2 * a)
                steps.append("x₁ = (-$b ± √($discriminant)) / ${2 * a}\n")
                steps.append("x₁ = $realPart + ${imagPart}i\n")
                steps.append("x₂ = $realPart - ${imagPart}i\n")
                result = "Complex Roots: x₁ = %.4f + %.4fi, x₂ = %.4f - %.4fi".format(realPart, imagPart, realPart, imagPart)
            }
        }

        // Step 4: Vertex
        steps.append("\nStep 3: Calculate the vertex\n")
        val vertexX = -b / (2 * a)
        val vertexY = a * vertexX * vertexX + b * vertexX + c
        steps.append("x = -b / (2a) = -${b} / ${2 * a} = $vertexX\n")
        steps.append("y = ax² + bx + c = ${a}($vertexX)² + ${b}($vertexX) + $c = $vertexY\n")
        steps.append("Vertex: ($vertexX, $vertexY)\n")

        resultView.text = result
        stepsView.text = steps.toString()

        // Save to database
        viewModel.insert(Equation(0, a, b, c, result, steps.toString()))

        // Plot graph
        plotGraph(a, b, c)
    }

    private fun plotGraph(a: Double, b: Double, c: Double) {
        val entries = mutableListOf<Entry>()
        for (x in -100..100) {
            val xVal = x / 10.0f
            val yVal = (a * xVal * xVal + b * xVal + c).toFloat()
            entries.add(Entry(xVal, yVal))
        }

        val dataSet = LineDataSet(entries, "y = ${a}x² + ${b}x + $c").apply {
            color = Color.parseColor("#00FFDD")
            setDrawCircles(false)
            lineWidth = 2f
        }

        chart.data = LineData(dataSet)
        chart.description.text = "Quadratic Function"
        chart.setBackgroundColor(Color.parseColor("#1F1B24"))
        chart.setGridBackgroundColor(Color.parseColor("#1F1B24"))
        chart.xAxis.textColor = Color.WHITE
        chart.axisLeft.textColor = Color.WHITE
        chart.axisRight.textColor = Color.WHITE
        chart.legend.textColor = Color.WHITE
        chart.animateY(1000)
        chart.invalidate()
    }

    private fun exportHistory() {
        viewModel.allEquations.value?.let { equations ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val jsonArray = JSONArray()
                    equations.forEach { equation ->
                        val jsonObject = JSONObject().apply {
                            put("a", equation.a)
                            put("b", equation.b)
                            put("c", equation.c)
                            put("result", equation.result)
                            put("steps", equation.steps)
                        }
                        jsonArray.put(jsonObject)
                    }

                    val fileName = "quadratic_history_${System.currentTimeMillis()}.json"
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val file = File(downloadsDir, fileName)
                    FileWriter(file).use { it.write(jsonArray.toString(2)) }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "History exported to Downloads/$fileName", Toast.LENGTH_LONG).show()
                    }
                } catch (e: IOException) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } ?: Toast.makeText(this, "No history to export.", Toast.LENGTH_SHORT).show()
    }
}