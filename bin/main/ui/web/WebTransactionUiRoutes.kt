package ui.web

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable
import signing.SignTransferRequest
import signing.TransactionSigner
import ui.TransactionSubmissionGateway

@Serializable
data class WebTransferRequest(
    val privateKey: String,
    val to: String,
    val amount: Int,
    val timestamp: Long? = null
)

@Serializable
data class WebTransferResponse(
    val status: String,
    val message: String
)

fun Routing.configureWebTransactionUi(
    signer: TransactionSigner,
    submissionGateway: TransactionSubmissionGateway
) {
    get("/ui") {
        call.respondText(
            contentType = ContentType.Text.Html,
            text = WEB_UI_HTML
        )
    }

    post("/ui/transactions") {
        val request = try {
            call.receive<WebTransferRequest>()
        } catch (_: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                WebTransferResponse(
                    status = "error",
                    message = "Malformed JSON body"
                )
            )
            return@post
        }

        if (request.privateKey.isBlank() || request.to.isBlank() || request.amount <= 0) {
            call.respond(
                HttpStatusCode.BadRequest,
                WebTransferResponse(
                    status = "error",
                    message = "privateKey, to and positive amount are required"
                )
            )
            return@post
        }

        val signedTx = runCatching {
            signer.signTransfer(
                SignTransferRequest(
                    privateKey = request.privateKey.trim(),
                    to = request.to.trim(),
                    amount = request.amount,
                    timestamp = request.timestamp
                )
            )
        }.getOrElse { error ->
            call.respond(
                HttpStatusCode.BadRequest,
                WebTransferResponse(
                    status = "error",
                    message = "Sign failed: ${error.message ?: "unknown error"}"
                )
            )
            return@post
        }

        val result = submissionGateway.submit(signedTx)
        val statusCode = if (result.success) HttpStatusCode.OK else HttpStatusCode.BadRequest
        val responseStatus = if (result.success) "ok" else "error"

        call.respond(
            statusCode,
            WebTransferResponse(
                status = responseStatus,
                message = result.message
            )
        )
    }
}

private val WEB_UI_HTML = """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Node UI</title>
  <style>
    body { font-family: Arial, sans-serif; margin: 2rem; max-width: 720px; }
    h1 { margin-bottom: 0.5rem; }
    p { color: #444; }
    .tabs { display: flex; gap: 0.5rem; margin-bottom: 1rem; }
    .tab { padding: 0.5rem 0.9rem; border: 1px solid #bbb; background: #f5f5f5; cursor: pointer; }
    .tab.active { background: #2f6feb; color: white; border-color: #2f6feb; }
    .panel { display: none; }
    .panel.active { display: block; }
    form { display: grid; gap: 0.75rem; }
    label { font-weight: 600; }
    input { width: 100%; padding: 0.5rem; box-sizing: border-box; }
    button { padding: 0.6rem 1rem; width: fit-content; cursor: pointer; }
    .result { margin-top: 1rem; padding: 0.75rem; border-radius: 4px; display: none; }
    .ok { background: #e7f8ed; color: #116329; }
    .error { background: #fdecec; color: #8e1b1b; }
  </style>
</head>
<body>
  <h1>Node UI</h1>

  <div class="tabs">
    <button type="button" class="tab active" data-target="txPanel">Send Transaction</button>
    <button type="button" class="tab" data-target="balancePanel">Check Balance</button>
  </div>

  <section id="txPanel" class="panel active">
    <p>Signs and submits a TRANSFER transaction using the values you provide.</p>

    <form id="txForm">
      <div>
        <label for="privateKey">Private Key (hex)</label>
        <input id="privateKey" name="privateKey" autocomplete="off" required />
      </div>

      <div>
        <label for="to">To Address</label>
        <input id="to" name="to" autocomplete="off" required />
      </div>

      <div>
        <label for="amount">Amount</label>
        <input id="amount" name="amount" type="number" min="1" required />
      </div>

      <div>
        <label for="timestamp">Timestamp (optional, ms)</label>
        <input id="timestamp" name="timestamp" type="number" min="1" />
      </div>

      <button type="submit">Sign and Send</button>
    </form>

    <div id="txResult" class="result"></div>
  </section>

  <section id="balancePanel" class="panel">
    <p>Checks current chain balance for an address using <code>GET /balance?address=...</code>.</p>

    <form id="balanceForm">
      <div>
        <label for="balanceAddress">Address</label>
        <input id="balanceAddress" name="balanceAddress" autocomplete="off" required />
      </div>

      <button type="submit">Check Balance</button>
    </form>

    <div id="balanceResult" class="result"></div>
  </section>

  <script>
    const tabs = document.querySelectorAll('.tab');
    const panels = document.querySelectorAll('.panel');
    const txForm = document.getElementById('txForm');
    const txResult = document.getElementById('txResult');
    const balanceForm = document.getElementById('balanceForm');
    const balanceResult = document.getElementById('balanceResult');

    function showResult(element, ok, message) {
      element.style.display = 'block';
      element.className = 'result ' + (ok ? 'ok' : 'error');
      element.textContent = message;
    }

    tabs.forEach((tab) => {
      tab.addEventListener('click', () => {
        const target = tab.getAttribute('data-target');

        tabs.forEach((t) => t.classList.remove('active'));
        panels.forEach((panel) => panel.classList.remove('active'));

        tab.classList.add('active');
        document.getElementById(target).classList.add('active');
      });
    });

    txForm.addEventListener('submit', async (event) => {
      event.preventDefault();

      const payload = {
        privateKey: document.getElementById('privateKey').value.trim(),
        to: document.getElementById('to').value.trim(),
        amount: Number(document.getElementById('amount').value),
      };

      const timestampValue = document.getElementById('timestamp').value.trim();
      if (timestampValue.length > 0) {
        payload.timestamp = Number(timestampValue);
      }

      try {
        const response = await fetch('/ui/transactions', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload),
        });

        const body = await response.json();
        showResult(txResult, body.status === 'ok', body.message || 'No message');
      } catch (error) {
        showResult(txResult, false, 'Request failed: ' + (error.message || 'unknown error'));
      }
    });

    balanceForm.addEventListener('submit', async (event) => {
      event.preventDefault();

      const address = document.getElementById('balanceAddress').value.trim();
      if (!address) {
        showResult(balanceResult, false, 'Address is required');
        return;
      }

      try {
        const response = await fetch('/balance?address=' + encodeURIComponent(address));
        const body = await response.json();

        if (response.ok && typeof body.balance === 'number') {
          showResult(balanceResult, true, 'Balance for ' + body.address + ': ' + body.balance);
          return;
        }

        if (body && body.error && body.error.message) {
          showResult(balanceResult, false, body.error.message);
          return;
        }

        showResult(balanceResult, false, 'Could not read balance response');
      } catch (error) {
        showResult(balanceResult, false, 'Request failed: ' + (error.message || 'unknown error'));
      }
    });
  </script>
</body>
</html>
""".trimIndent()


