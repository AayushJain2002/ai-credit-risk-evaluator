import { useState } from "react";

function App() {
  const [form, setForm] = useState({
    income: "",
    creditScore: "",
    employmentStatus: "",
  });

  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async () => {
    setResult(null);
    setError(null);
    setLoading(true);

    try {
      const response = await fetch("http://127.0.0.1:8000/analyze", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          income: Number(form.income),
          creditScore: Number(form.creditScore),
          employmentStatus: form.employmentStatus,
        }),
      });

      const data = await response.json();

      if (!response.ok) {
        setError(data);
        return;
      }

      setResult(data);
    } catch (err) {
      console.error("Fetch error:", err);
      setError({ error: "Request failed" });
    } finally {
      setLoading(false);
    }
  };

  const getDecisionColor = (decision) => {
    if (decision === "APPROVE") return "green";
    if (decision === "REVIEW") return "orange";
    return "red";
  };

  return (
    <div style={{ padding: "30px", fontFamily: "Arial" }}>
      <h1>AI Credit Risk Evaluator</h1>

      {/* -------- INPUT FORM -------- */}
      <div style={{ marginBottom: "20px" }}>
        <div>
          <label>Income</label>
          <br />
          <input
            name="income"
            type="number"
            value={form.income}
            onChange={handleChange}
          />
        </div>

        <div style={{ marginTop: "10px" }}>
          <label>Credit Score</label>
          <br />
          <input
            name="creditScore"
            type="number"
            value={form.creditScore}
            onChange={handleChange}
          />
        </div>

        <div style={{ marginTop: "10px" }}>
          <label>Employment Status</label>
          <br />
          <select
            name="employmentStatus"
            value={form.employmentStatus}
            onChange={handleChange}
          >
            <option value="">Select status</option>
            <option value="employed">Employed</option>
            <option value="self-employed">Self Employed</option>
            <option value="unemployed">Unemployed</option>
          </select>
        </div>

        <button
          onClick={handleSubmit}
          disabled={loading}
          style={{ marginTop: "15px", padding: "8px 16px" }}
        >
          {loading ? "Evaluating..." : "Evaluate"}
        </button>
      </div>

      {/* -------- RESULT -------- */}
      {result && !error && (
        <div style={{ borderTop: "1px solid #ccc", paddingTop: "20px" }}>
          <h2 style={{ color: getDecisionColor(result.decision) }}>
            {result.decision}
          </h2>

          <p>
            <strong>Risk Score:</strong>{" "}
            {(result.riskScore * 100).toFixed(0)}%
          </p>

          <h3>Key Factors</h3>
          <ul>
            {result.reasons.map((r, i) => (
              <li key={i}>{r}</li>
            ))}
          </ul>

          <h3>Explanation</h3>
          <p>{result.explanation}</p>

          <h3>Suggestions</h3>
          <ul>
            {result.suggestions.map((s, i) => (
              <li key={i}>{s}</li>
            ))}
          </ul>
        </div>
      )}

      {/* -------- ERROR -------- */}
      {error && (
        <div style={{ color: "red" }}>
          <h3>Error</h3>
          <pre>{JSON.stringify(error, null, 2)}</pre>
        </div>
      )}
    </div>
  );
}

export default App;