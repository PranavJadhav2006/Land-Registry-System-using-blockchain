// controllers/landController.js
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";
import { ethers , Wallet } from "ethers";
import dotenv from "dotenv";
import _ from "lodash";
import multer from "multer";
import crypto from "crypto";

import LandEvent from "../models/LandEvent.js";
import LandParcel from "../models/LandParcel.js";

dotenv.config();

// ⚙️ Emulate __dirname in ES modules
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// Load ABI artifact
const artifact = JSON.parse(
  fs.readFileSync(path.join(__dirname, "../abi/OpenAcres.json"), "utf8")
);

// Setup ethers.js contract instance
const provider = new ethers.providers.JsonRpcProvider(process.env.RPC_URL);
const wallet = new ethers.Wallet(process.env.OWNER_PRIVATE_KEY, provider);
const contract = new ethers.Contract(
  process.env.CONTRACT_ADDRESS_LOCAL,
  artifact.abi,
  wallet
);

// Multer storage configuration
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    const uploadPath = path.join(__dirname, "../uploads");
    fs.mkdirSync(uploadPath, { recursive: true }); // Create directory if it doesn't exist
    cb(null, uploadPath);
  },
  filename: (req, file, cb) => {
    cb(null, `${Date.now()}-${file.originalname}`);
  },
});

const upload = multer({ storage });

export async function mintLand(req, res) {
  try {
    const {
      surveyNumber,
      ownerAddress,
      location,
      area,
      propertyType,
      marketValue
    } = req.body;

    if (!surveyNumber || !ownerAddress) {
      return res.status(400).json({
        success: false,
        error: "Missing required fields (surveyNumber, ownerAddress)"
      });
    }

    // ✅ Perform blockchain mint (assuming your contract method is updated to support these fields)
    // Replace below with your actual mint logic
    const tx = await contract.mintLand(
      ownerAddress,
      location,
      surveyNumber,
      area,
      propertyType,
      marketValue
    );
    const receipt = await tx.wait();

    const event = contract.interface.getEvent("LandMinted");
    const log = receipt.logs.find(
      l => l.topics[0] === contract.interface.getEventTopic(event)
    );

    if (!log) {
      throw new Error("Minting event not found in transaction logs");
    }

    const decoded = contract.interface.decodeEventLog(event, log.data, log.topics);
    const tokenId = decoded.tokenId.toString();

    // ✅ Save to DB
    const landRecord = await LandEvent.create({
      tokenId,
      surveyNumber,
      ownerAddress,
      location,
      area,
      propertyType,
      marketValue,
      createdAt: new Date()
    });

    res.json({
      success: true,
      tokenId,
      txHash: tx.hash,
      landRecord
    });

  } catch (err) {
    console.error("Minting error:", err);
    res.status(500).json({
      success: false,
      error: err.message
    });
  }
}

export async function confirmTransfer(req, res) {
  try {
    const { tokenId, from, to } = req.body;

    if (!tokenId || !from || !to) {
      return res.status(400).json({
        success: false,
        error: "Missing required fields (tokenId, from, to)"
      });
    }

    const normalizedFrom = ethers.utils.getAddress(from);
    const normalizedTo = ethers.utils.getAddress(to);

    // Find the land in the database
    const land = await LandEvent.findOne({
      tokenId: tokenId.toString(),
      currentOwner: normalizedTo // The new owner should be the current owner after transfer
    });

    if (!land) {
      return res.status(404).json({
        success: false,
        error: "Land not found or not owned by the recipient"
      });
    }

    // Optionally, add more checks here if needed, e.g., verify the transfer was initiated

    res.json({
      success: true,
      message: "Transfer confirmed and recorded successfully",
      landRecord: _.pick(land, ['tokenId', 'currentOwner', 'name', 'txHash'])
    });

  } catch (err) {
    console.error("❌ Confirm Transfer error:", err);
    res.status(500).json({
      success: false,
      error: err.reason || err.message,
      ...(process.env.NODE_ENV === 'development' && { stack: err.stack })
    });
  }
}

export const uploadMiddleware = upload.single("document");

export async function uploadLandDocuments(req, res) {
  try {
    const { surveyNumber } = req.params;
    const { file } = req;

    if (!file) {
      return res.status(400).json({ success: false, error: "No file uploaded." });
    }

    const landParcel = await LandParcel.findOne({ surveyNumber });

    if (!landParcel) {
      return res.status(404).json({ success: false, error: "Land parcel not found." });
    }

    // Read file to create a hash
    const fileBuffer = fs.readFileSync(file.path);
    const hash = crypto.createHash('sha256').update(fileBuffer).digest('hex');

    const newDocument = {
      fileName: file.originalname,
      fileHash: hash,
      uploadedAt: new Date(),
    };

    landParcel.documents.push(newDocument);
    await landParcel.save();

    res.json({
      success: true,
      message: "Document uploaded successfully.",
      document: newDocument,
    });
  } catch (err) {
    console.error("❌ Upload Land Documents error:", err);
    res.status(500).json({
      success: false,
      error: err.message,
    });
  }
}

export async function listDocumentsForParcel(req, res) {
  try {
    const { surveyNumber } = req.params;
    const landParcel = await LandParcel.findOne({ surveyNumber });

    if (!landParcel) {
      return res.status(404).json({ success: false, error: "Land parcel not found." });
    }

    res.json({
      success: true,
      documents: landParcel.documents,
    });
  } catch (err) {
    console.error("❌ List Documents For Parcel error:", err);
    res.status(500).json({
      success: false,
      error: err.message,
    });
  }
}

export async function searchLand(req, res) {
  try {
    // Placeholder for searchLand logic
    res.status(501).json({ success: false, message: "searchLand not implemented" });
  } catch (err) {
    console.error("❌ Search Land error:", err);
    res.status(500).json({
      success: false,
      error: err.reason || err.message,
      ...(process.env.NODE_ENV === 'development' && { stack: err.stack })
    });
  }
}

export async function transferLand(req, res) {
  try {
    const { from, to, surveyNumber } = req.body; // Changed from tokenId to surveyNumber

    if (!from || !to || !surveyNumber) { // Check for surveyNumber
      return res.status(400).json({
        success: false,
        error: "Missing required fields (from, to, surveyNumber)"
      });
    }

    const normalizedFrom = ethers.utils.getAddress(from);
    const normalizedTo = ethers.utils.getAddress(to);

    // 🔍 1. Retrieve Land from DB using surveyNumber
    const land = await LandEvent.findOne({
      surveyNumber: surveyNumber, // Find by surveyNumber
      ownerAddress: normalizedFrom // Find by ownerWallet
    });

    if (!land) {
      return res.status(404).json({
        success: false,
        error: "Land with specified survey number not found or not owned by sender"
      });
    }

    const tokenId = land.tokenId.toString(); // Get the tokenId from the land record

    // 🔐 2. Determine signer (No change here)
    let signer;
    const knownAddresses = {
      [process.env.TEST_ACCOUNT_1.toLowerCase()]: process.env.TEST_ACCOUNT_1_PRIVATE_KEY,
      [process.env.TEST_ACCOUNT_2.toLowerCase()]: process.env.TEST_ACCOUNT_2_PRIVATE_KEY,
      ["0xf39fd6e51aad88f6f4ce6ab8827279cfffb92266"]: process.env.OWNER_PRIVATE_KEY
    };

    const privKey = knownAddresses[normalizedFrom.toLowerCase()];
    if (!privKey) {
      return res.status(403).json({ success: false, error: "Unauthorized sender address" });
    }

    signer = new ethers.Wallet(privKey, provider);
    const contractWithSigner = contract.connect(signer);

    // ✅ 3. Check On-chain ownership (No change here)
    const onChainOwner = await contractWithSigner.ownerOf(tokenId);
    if (ethers.utils.getAddress(onChainOwner) !== normalizedFrom) {
      return res.status(403).json({
        success: false,
        error: "On-chain ownership verification failed"
      });
    }

    // 🚀 4. Transfer Land (No change here)
    const tx = await contractWithSigner.transferLand(normalizedFrom, normalizedTo, tokenId);
    await tx.wait();

    // 🧠 5. Update MongoDB (No change here)
    const updated = await LandEvent.findOneAndUpdate(
      { tokenId },
      {
        currentOwner: normalizedTo,
        $push: {
          transferHistory: {
            from: normalizedFrom,
            to: normalizedTo,
            txHash: tx.hash,
            timestamp: new Date()
          }
        }
      },
      { new: true }
    );

    if (!updated) {
      console.warn("⚠️ Transfer succeeded but DB update failed for token:", tokenId);
    }

    res.json({
      success: true,
      tokenId,
      txHash: tx.hash,
      newOwner: normalizedTo,
      previousOwner: normalizedFrom,
      landRecord: _.pick(updated, ['tokenId', 'currentOwner', 'name', 'txHash'])
    });

  } catch (err) {
    console.error("❌ Transfer error:", err);
    res.status(500).json({
      success: false,
      error: err.reason || err.message,
      ...(process.env.NODE_ENV === 'development' && { stack: err.stack })
    });
  }
}

export async function flagDispute(req, res) {
  try {
    const { tokenId, reporterAddress, reason } = req.body;

    if (!tokenId || !reporterAddress || !reason) {
      return res.status(400).json({
        success: false,
        error: "Missing required fields (tokenId, reporterAddress, reason)"
      });
    }

    const normalizedReporterAddress = ethers.utils.getAddress(reporterAddress);

    // Find the land in the database
    const land = await LandEvent.findOne({
      tokenId: tokenId.toString()
    });

    if (!land) {
      return res.status(404).json({
        success: false,
        error: "Land not found"
      });
    }

    // Add the dispute to the land record
    land.disputes.push({
      reporter: normalizedReporterAddress,
      reason: reason,
      timestamp: new Date()
    });

    await land.save();

    res.json({
      success: true,
      message: "Dispute flagged successfully",
      landRecord: _.pick(land, ['tokenId', 'currentOwner', 'name', 'disputes'])
    });

  } catch (err) {
    console.error("❌ Flag Dispute error:", err);
    res.status(500).json({
      success: false,
      error: err.reason || err.message,
      ...(process.env.NODE_ENV === 'development' && { stack: err.stack })
    });
  }
}

export async function getLandDetails(req, res) {
  try {
    const { tokenId } = req.params;

    if (!tokenId) {
      return res.status(400).json({
        success: false,
        error: "Missing required parameter: tokenId"
      });
    }

    const land = await LandEvent.findOne({
      tokenId: tokenId.toString()
    });

    if (!land) {
      return res.status(404).json({
        success: false,
        error: "Land not found"
      });
    }

    res.json({
      success: true,
      landDetails: _.pick(land, ['tokenId', 'currentOwner', 'name', 'description', 'image', 'external_url', 'attributes', 'transferHistory'])
    });

  } catch (err) {
    console.error("❌ Get Land Details error:", err);
    res.status(500).json({
      success: false,
      error: err.reason || err.message,
      ...(process.env.NODE_ENV === 'development' && { stack: err.stack })
    });
  }
}

export async function getOwnershipAndDisputeHistory(req, res) {
  try {
    const { tokenId } = req.params;

    if (!tokenId) {
      return res.status(400).json({
        success: false,
        error: "Missing required parameter: tokenId"
      });
    }

    const land = await LandEvent.findOne({
      tokenId: tokenId.toString()
    });

    if (!land) {
      return res.status(404).json({
        success: false,
        error: "Land not found"
      });
    }

    res.json({
      success: true,
      ownershipHistory: land.transferHistory,
      disputeHistory: land.disputes || []
    });

  } catch (err) {
    console.error("❌ Get Ownership and Dispute History error:", err);
    res.status(500).json({
      success: false,
      error: err.reason || err.message,
      ...(process.env.NODE_ENV === 'development' && { stack: err.stack })
    });
  }
}

export async function getTransferStatus(req, res) {
  try {
    const { txHash } = req.params;

    if (!txHash) {
      return res.status(400).json({
        success: false,
        error: "Missing required parameter: txHash"
      });
    }

    // In a real application, you would query the blockchain or a transaction indexer
    // to get the actual status of the transaction. For this example, we'll simulate it.
    const receipt = await provider.getTransactionReceipt(txHash);

    if (!receipt) {
      return res.status(404).json({
        success: false,
        error: "Transaction not found on the blockchain"
      });
    }

    res.json({
      success: true,
      txHash: txHash,
      status: receipt.status === 1 ? "confirmed" : "failed",
      blockNumber: receipt.blockNumber,
      gasUsed: receipt.gasUsed.toString()
    });
  }
  catch (err) {
    console.error("❌ Get Transfer Status error:", err);
    res.status(500).json({
      success: false,
      error: err.reason || err.message,
      ...(process.env.NODE_ENV === 'development' && { stack: err.stack })
    });
  }
}

export async function initiateTransfer(req, res) {
  try {
    const { from, to, tokenId } = req.body;

    if (!from || !to || !tokenId) {
      return res.status(400).json({
        success: false,
        error: "Missing required fields (from, to, tokenId)"
      });
    }

    const normalizedFrom = ethers.utils.getAddress(from);
    const normalizedTo = ethers.utils.getAddress(to);

    // Find the land in the database
    const land = await LandEvent.findOne({
      tokenId: tokenId.toString(),
      currentOwner: normalizedFrom
    });

    if (!land) {
      return res.status(404).json({
        success: false,
        error: "Land not found or not owned by the sender"
      });
    }

    // Here you would typically interact with your smart contract to initiate the transfer.
    // For this example, we'll just return a success message.

    res.json({
      success: true,
      message: "Transfer initiated successfully",
      tokenId: tokenId,
      from: normalizedFrom,
      to: normalizedTo
    });

  } catch (err) {
    console.error("❌ Initiate Transfer error:", err);
    res.status(500).json({
      success: false,
      error: err.reason || err.message,
      ...(process.env.NODE_ENV === 'development' && { stack: err.stack })
    });
  }
}

export async function listAllLands(req, res) {
  try {
    const lands = await LandEvent.find({});
    res.json({
      success: true,
      lands: lands.map(land => _.pick(
        land, [
          'tokenId',
          'surveyNumber',
          'ownerAddress',
          'location',
          'area',
          'propertyType',
          'marketValue',
          'status',
          'createdAt'
        ]
      ))
    });
  } catch (err) {
    console.error("❌ List All Lands error:", err);
    res.status(500).json({
      success: false,
      error: err.reason || err.message,
      ...(process.env.NODE_ENV === 'development' && { stack: err.stack })
    });
  }
}

export async function registerLand(req, res) {
    // Placeholder for registerLand logic
    try {
    const {
      surveyNumber,
      ownerAddress,
      location,
      area,
      propertyType,
      marketValue
    } = req.body;

    if (!surveyNumber || !ownerAddress) {
      return res.status(400).json({
        success: false,
        error: "Missing required fields (surveyNumber, ownerAddress)"
      });
    }

    // ✅ Perform blockchain mint (assuming your contract method is updated to support these fields)
    // Replace below with your actual mint logic
    const tx = await contract.mintLand(
      ownerAddress,
      location,
      surveyNumber,
      area,
      propertyType,
      marketValue
    );
    const receipt = await tx.wait();

    const event = contract.interface.getEvent("LandMinted");
    const log = receipt.logs.find(
      l => l.topics[0] === contract.interface.getEventTopic(event)
    );

    if (!log) {
      throw new Error("Minting event not found in transaction logs");
    }

    const decoded = contract.interface.decodeEventLog(event, log.data, log.topics);
    const tokenId = decoded.tokenId.toString();

    // ✅ Save to DB
    const landRecord = await LandEvent.create({
      tokenId,
      surveyNumber,
      ownerAddress,
      location,
      area,
      propertyType,
      marketValue,
      createdAt: new Date()
    });

    res.json({
      success: true,
      tokenId,
      txHash: tx.hash,
      landRecord
    });
    res.status(501).json({ success: false, message: "registerLand not implemented" });
  } catch (err) {
    console.error("❌ Register Land error:", err);
    res.status(500).json({
      success: false,
      error: err.reason || err.message,
      ...(process.env.NODE_ENV === 'development' && { stack: err.stack })
    });
  }
}



