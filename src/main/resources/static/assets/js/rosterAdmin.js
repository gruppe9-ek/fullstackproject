// Minimal, men fuldt fungerende admin-UI til US18

const ROLES = ["KIOSK","TICKET_SALES","TICKET_CHECK","OPERATOR","CLEANING"];

const els = {
    mondayInput: document.getElementById("mondayInput"),
    prevWeekBtn: document.getElementById("prevWeekBtn"),
    nextWeekBtn: document.getElementById("nextWeekBtn"),
    copyPrevBtn: document.getElementById("copyPrevBtn"),
    tableBody: document.querySelector("#rosterTable tbody"),
    flash: document.getElementById("flash"),
    assignForm: document.getElementById("assignForm"),
    assignDate: document.getElementById("assignDate"),
    assignStaffId: document.getElementById("assignStaffId"),
    assignRole: document.getElementById("assignRole"),
};

function setFlash(msg, type="info") {
    els.flash.textContent = msg || "";
    els.flash.className = type; // brug evt. jeres egen klasse fra moviesAdmin.css
}

function fmt(d) { return d.toISOString().slice(0,10); }

function mondayOf(date) {
    const d = new Date(date);
    const day = (d.getDay() + 6) % 7; // mandag=0
    d.setDate(d.getDate() - day);
    d.setHours(12,0,0,0);
    return d;
}

function toDate(s) {
    // s = "YYYY-MM-DD"
    const [y,m,d] = s.split("-").map(Number);
    return new Date(Date.UTC(y, m-1, d, 12, 0, 0));
}

async function loadWeek(mondayStr) {
    els.tableBody.innerHTML = "";
    setFlash("Henter uge…");

    const res = await fetch(`/api/roster?monday=${encodeURIComponent(mondayStr)}`);
    if (!res.ok) {
        setFlash(`Kunne ikke hente uge (${res.status})`, "error");
        return;
    }
    const shifts = await res.json();

    // sortér dato/rolle pænt
    shifts.sort((a,b) => a.date.localeCompare(b.date) || a.role.localeCompare(b.role));

    for (const sh of shifts) {
        const tr = document.createElement("tr");

        const tdDate = document.createElement("td");
        tdDate.textContent = sh.date;

        const tdStaff = document.createElement("td");
        tdStaff.textContent = sh.staff ? `${sh.staff.name} (#${sh.staff.staffId})` : "-";

        const tdRole = document.createElement("td");
        tdRole.textContent = sh.role;

        const tdAct = document.createElement("td");
        const sel = document.createElement("select");
        for (const r of ROLES) {
            const opt = document.createElement("option");
            opt.value = r; opt.textContent = r;
            if (r === sh.role) opt.selected = true;
            sel.appendChild(opt);
        }
        const btn = document.createElement("button");
        btn.textContent = "Skift rolle";
        btn.addEventListener("click", async () => {
            const newRole = sel.value;
            const r = await fetch(`/api/roster/shift/${sh.shiftId}/role?role=${encodeURIComponent(newRole)}`, {
                method: "PATCH"
            });
            if (r.ok) {
                setFlash("Rolle opdateret.", "success");
                await loadWeek(els.mondayInput.value);
            } else {
                const err = await r.json().catch(() => ({}));
                setFlash(err.message || `Fejl ved skift af rolle (${r.status})`, "error");
            }
        });

        tdAct.appendChild(sel);
        tdAct.appendChild(document.createTextNode(" "));
        tdAct.appendChild(btn);

        tr.append(tdDate, tdStaff, tdRole, tdAct);
        els.tableBody.appendChild(tr);
    }

    setFlash(shifts.length ? "Uge indlæst." : "Ingen vagter i denne uge.");
}

function populateRoleSelects() {
    els.assignRole.innerHTML = "";
    for (const r of ROLES) {
        const opt = document.createElement("option");
        opt.value = r; opt.textContent = r;
        els.assignRole.appendChild(opt);
    }
}

function wireEvents() {
    els.prevWeekBtn.addEventListener("click", () => {
        const d = toDate(els.mondayInput.value);
        d.setDate(d.getDate() - 7);
        els.mondayInput.value = fmt(d);
        loadWeek(els.mondayInput.value);
    });

    els.nextWeekBtn.addEventListener("click", () => {
        const d = toDate(els.mondayInput.value);
        d.setDate(d.getDate() + 7);
        els.mondayInput.value = fmt(d);
        loadWeek(els.mondayInput.value);
    });

    els.copyPrevBtn.addEventListener("click", async () => {
        const toMon = els.mondayInput.value;
        const d = toDate(toMon);
        d.setDate(d.getDate() - 7);
        const fromMon = fmt(d);

        setFlash("Kopierer forrige uge…");
        const res = await fetch(`/api/roster/copy?fromMonday=${fromMon}&toMonday=${toMon}`, { method: "POST" });
        if (res.ok) {
            const data = await res.json();
            setFlash(`Kopieret. Oprettet: ${data.created}, Skippet: ${data.skipped}`, "success");
            loadWeek(toMon);
        } else {
            const err = await res.json().catch(() => ({}));
            setFlash(err.message || `Fejl ved kopiering (${res.status})`, "error");
        }
    });

    els.assignForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        const date = els.assignDate.value;
        const staffId = els.assignStaffId.value;
        const role = els.assignRole.value;

        if (!date || !staffId || !role) {
            setFlash("Udfyld dato, staffId og rolle.", "error");
            return;
        }

        setFlash("Opretter vagt…");
        const res = await fetch(`/api/roster/assign?date=${date}&staffId=${staffId}&role=${role}`, { method: "POST" });
        if (res.ok) {
            setFlash("Vagt oprettet.", "success");
            els.assignForm.reset();
            // sæt dato tilbage til den viste uge for nemhed
            els.assignDate.value = document.querySelector("#mondayInput").value;
            loadWeek(els.mondayInput.value);
        } else {
            const err = await res.json().catch(() => ({}));
            setFlash(err.message || `Fejl ved oprettelse (${res.status})`, "error");
        }
    });
}

// Init
(function init() {
    populateRoleSelects();

    // sæt default til denne uges mandag
    const today = new Date();
    const mon = mondayOf(today);
    els.mondayInput.value = fmt(mon);
    els.assignDate.value = fmt(today);

    wireEvents();
    loadWeek(els.mondayInput.value);
})();